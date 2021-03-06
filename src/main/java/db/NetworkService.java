package db;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.stream.Collectors;
import java.util.*;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import model.Config;
import model.Node;
import model.SearchQuery;
import model.SearchResult;
import model.Statistics;

/**
 * Created by Thilini on 11/5/2017.
 */

public class NetworkService {
	final static private Logger logger = Logger.getLogger(NetworkService.class);

	private DatagramSocket socket;
	private int receivedMessages, sentMessages, unAnsweredMessages;
	private List<Integer> latencyArray = new ArrayList<>();
	private List<Integer> hopArray = new ArrayList<>();
	private DecimalFormat formatter = new DecimalFormat("0000");

	final private MovieController movieController = MovieController.getInstance("../../resources/File Names.txt");

	private final List<Node> neighbours = new ArrayList<>();
	private final List<SearchQuery> searchQueryList = new ArrayList<>();
	private List<SearchResult> searchResultList = new ArrayList<>();
	private final Node myNode = new Node();

	public NetworkService() {
		BasicConfigurator.configure();

	}

	public void update() throws IOException {
		int MINUTES = 1; // The delay in minutes
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() { // Function runs every MINUTES minutes.
				// Run the code you want here
				neighbours.forEach((a) -> {
					a.setStatus("InActive");
					String timeStamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date());
					a.setUpdateTime(timeStamp);
					String pingMsg = Config.PING + " " + Config.IP + " " + Config.PORT;
					sender(pingMsg, a);
				});
				// CLASSB.funcb(); // If the function you wanted was static
			}
		}, 0, 1000 * 60 * MINUTES);

	}

	public void run() throws IOException {

		boolean done = true;
		while (true) {
			if (done) {
				socket = new DatagramSocket(Config.PORT);
				String msg = Config.REG + " " + Config.IP + " " + Config.PORT + " " + Config.USERNAME;

				this.myNode.setIP_address(Config.IP);
				this.myNode.setPort_no(Config.PORT);
				sender(msg);
				done = false;
			}
			byte[] buffer = new byte[65536];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			try {
				socket.receive(packet);
				byte[] data = packet.getData();
				String message = new String(data, 0, packet.getLength());

				logger.info("receiving ; " + message);
				receiver(message);
				// onResponseReceived(message,new
				// Node(packet.getAddress().getHostName(),packet.getPort()));

			} catch (IOException e) {
				logger.error(e);
			}

		}
	}

	public boolean unRegister() {
		String msg = Config.UNREG + " " + Config.IP + " " + Config.PORT + " " + Config.USERNAME;
		sender(msg);
		return true;
	}

	public boolean send_leave() {
		for (Node peer : neighbours) {
			String msg = Config.LEAVE + " " + Config.IP + " " + Config.PORT;
			sender(msg, new Node(peer.getIP_address(), peer.getPort_no()));
		}
		return true;
	}

	public boolean leave_network(Node node) {
		this.removeNeighbour(node);
		String msg = Config.LEAVEOK + " 0";
		sender(msg, new Node(node.getIP_address(), node.getPort_no()));
		return true;
	}

	public synchronized void startSearch(String queryText) {

		SearchQuery query = new SearchQuery();
		query.setOriginNode(myNode);
		query.setQueryText(queryText);
		query.setHops(0);
		query.setSenderNode(myNode);
		query.setTimestamp(System.currentTimeMillis());

		searchQueryList.add(query);

		List<String> movies = this.movieController.searchMovies(query.getQueryText());

		SearchResult result = new SearchResult();
		result.setOrginNode(myNode);
		result.setMovies(movies);
		result.setHops(0);
		result.setTimestamp(query.getTimestamp());

		List<Node> randoms = this.getRandom3nodes();
		for (Node peer : randoms) {
			searchRequest(peer, query);
			System.out.println(peer.getPort_no());
		}
		searchResponce(query.getOriginNode(), result);
	}

	public List<Node> getRandom3nodes() {
		if (neighbours.size() <= 3) {
			return neighbours;
		} else {
			Random r = new Random();
			int Low = 0;
			int High = neighbours.size();
			int random_1 = r.nextInt(High - Low) + Low;
			int random_2 = r.nextInt(High - Low) + Low;
			int random_3 = r.nextInt(High - Low) + Low;
			while (random_1 == random_2) {
				random_2 = r.nextInt(High - Low) + Low;
			}
			while (random_1 == random_3) {
				random_3 = r.nextInt(High - Low) + Low;
			}
			while (random_2 == random_3) {
				random_3 = r.nextInt(High - Low) + Low;
			}

			List<Node> randoms = new ArrayList<Node>();
			randoms.add(neighbours.get(random_1));
			randoms.add(neighbours.get(random_2));
			randoms.add(neighbours.get(random_3));
			return randoms;
		}
	}

	public boolean searchRequest(Node peer, SearchQuery query) {
		String msg = Config.SER + " " + Config.IP + " " + Config.PORT + " " + query.getQueryText() + " "
				+ query.getHops() + " " + query.getTimestamp() + " " + query.getOriginNode().getIP_address() + " "
				+ query.getOriginNode().getPort_no();
		sender(msg, new Node(peer.getIP_address(), peer.getPort_no()));
		return true;
	}

	public boolean searchResponce(Node originNode, SearchResult result) {
		String msg = Config.SEROK + " " + result.getMovies().size() + " " + Config.IP + " " + Config.PORT + " "
				+ result.getHops() + " " + result.getTimestamp();
		for (String m : result.getMovies()) {
			msg += " " + m;
		}
		sender(msg, originNode);
		return true;
	}

	private boolean checkQueryList(SearchQuery query) {
		for (SearchQuery q : searchQueryList) {
			if (q.getQueryText().equals(query.getQueryText()) && (q.getTimestamp() == query.getTimestamp())) {
				return true;
			}
		}
		return false;
	}

	synchronized private void search(SearchQuery query) {

		if (this.checkQueryList(query)) {
			unAnsweredMessages++;
			return;
		} else {
			searchQueryList.add(query);
		}

		// Increase the number of hops by one
		query.setHops(query.getHops() + 1);
		query.setSenderNode(myNode);

		Node sender = query.getSenderNode();

		List<String> results = movieController.searchMovies(query.getQueryText());

		SearchResult result = new SearchResult();
		result.setOrginNode(query.getOriginNode());
		result.setMovies(results);
		result.setHops(query.getHops());
		result.setTimestamp(query.getTimestamp());

		neighbours.stream().filter(peer -> !peer.equals(sender)).forEach(peer -> {
			searchRequest(peer, query);
		});
		logger.info("Result sent to " + query.getOriginNode());
		searchResponce(query.getOriginNode(), result);
	}

	private void sender(String msg) {
		String length_final = formatter.format(msg.length() + 5);
		String msg_final = length_final + " " + msg;
		try {
			DatagramPacket packet = new DatagramPacket(msg_final.getBytes(), msg_final.getBytes().length,
					InetAddress.getByName(Config.BOOTSTRAP_IP), Config.BOOTSTRAP_PORT);
			socket.send(packet);
			sentMessages++;
		} catch (IOException e) {
			logger.error(e);
		}
	}

	private void sender(String msg, Node node) {
		String length_final = formatter.format(msg.length() + 5);
		String msg_final = length_final + " " + msg;
		try {
			DatagramPacket packet = new DatagramPacket(msg_final.getBytes(), msg_final.getBytes().length,
					InetAddress.getByName(node.getIP_address()), node.getPort_no());
			socket.send(packet);
			sentMessages++;
		} catch (IOException e) {
			logger.error(e);
		}
	}

	// will be invoked when a response is received
	private void receiver(String message) {
		logger.info(message);
		receivedMessages++;
		StringTokenizer tokenizer = new StringTokenizer(message, " ");
		String length = tokenizer.nextToken();
		String command = tokenizer.nextToken();

		if (Config.REGOK.equals(command)) {
			int no_nodes = Integer.parseInt(tokenizer.nextToken());

			switch (no_nodes) {
			case 0:
				break;

			case 1:
				logger.info("registration is successful, 1 nodes contacts is returned");
				String ip = tokenizer.nextToken();
				int port = Integer.parseInt(tokenizer.nextToken());
				String status = "Active";
				String timeStamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date());

				Node node = new Node(ip, port, status, timeStamp);
				String msg = Config.JOIN + " " + Config.IP + " " + Config.PORT;
				sender(msg, node);
				addNeighbour(node);
				break;

			case 2:

				for (int i = 0; i < no_nodes; i++) {
					String host = tokenizer.nextToken();
					String hostport = tokenizer.nextToken();
					String hoststatus = "Active";
					String hosttimeStamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date());
					Node temp = new Node(host, Integer.parseInt(hostport), hoststatus, hosttimeStamp);
					String joinMsg = Config.JOIN + " " + Config.IP + " " + Config.PORT;
					sender(joinMsg, temp);
					addNeighbour(temp);
				}
				logger.info("registration is successful, 2 nodes' contacts are returned");
				break;
			case 9996:
				logger.info("Failed to register. BootstrapServer is full.");
				break;

			case 9997:
				logger.info("Failed to register. This ip and port is already used by another App.");
				// closeSocket();
				break;

			case 9998:
				logger.info("You are already registered. Please unregister first.");
				break;

			case 9999:
				logger.info("Error in the command. Please fix the error");
				// closeSocket();
				break;
			}

		} else if (Config.UNROK.equals(command)) {
			logger.info("Successfully unregistered this Node from the boostrap server");

		} else if (Config.JOIN.equals(command)) {
			String ip = tokenizer.nextToken();
			int port = Integer.parseInt(tokenizer.nextToken());
			String status = "Active";
			String timeStamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date());
			Node node = new Node(ip, port);
			node.setStatus(status);
			node.setUpdateTime(timeStamp);

			String joinokMsg = Config.JOINOK + " 0";
			sender(joinokMsg, node);
			addNeighbour(node);

		} else if (Config.JOINOK.equals(command)) {
			int result = Integer.parseInt(tokenizer.nextToken());
			switch (result) {
			case 0:
				logger.info("join successful");
				break;

			case 9999:
				logger.error("Error while adding new node to routing table");
				break;
			}

		} else if (Config.LEAVE.equals(command)) {
			String ip = tokenizer.nextToken();
			int port = Integer.parseInt(tokenizer.nextToken());
			Node node = new Node(ip, port);
			this.leave_network(node);

		} else if (Config.LEAVEOK.equals(command)) {
			int result = Integer.parseInt(tokenizer.nextToken());
			switch (result) {
			case 0:
				logger.info("Leaving network successful");
				break;

			case 9999:
				logger.error("Error while leaving the network");
				break;
			}

		} else if (Config.SER.equals(command)) {
			String ip = tokenizer.nextToken();
			int port = Integer.parseInt(tokenizer.nextToken());
			String query = tokenizer.nextToken();
			int hops = Integer.parseInt(tokenizer.nextToken());
			long timestamp = Long.parseLong(tokenizer.nextToken());
			String origin_ip = tokenizer.nextToken();
			int origin_port = Integer.parseInt(tokenizer.nextToken());
			
			logger.info("here come serch");
			logger.info(timestamp);

			search(new SearchQuery(new Node(origin_ip, origin_port),new Node(ip, port), query, hops, timestamp));

		} else if (Config.SEROK.equals(command)) {
			int no_files = Integer.parseInt(tokenizer.nextToken());
			String ip = tokenizer.nextToken();
			int port = Integer.parseInt(tokenizer.nextToken());
			int hops = Integer.parseInt(tokenizer.nextToken());
			long timestamp = Long.parseLong(tokenizer.nextToken());
			long latency = (System.currentTimeMillis() - timestamp);

			latencyArray.add((int) latency);
			hopArray.add(hops);

			List<String> movies = new ArrayList<>();

			for (int i = 0; i < no_files; i++)
				movies.add(tokenizer.nextToken());

			SearchResult result = new SearchResult(new Node(ip, port), movies, hops);
			int moviesCount = no_files;
			result.setMoviesCount(moviesCount);
			if (moviesCount > 0) {
				this.searchResultList.add(result);

				this.printSearchResults();
			}
			// String output = String.format("Number of movies: %d\nMovies:
			// %s\nHops: %d\nSender %s:%d\n", moviesCount,
			// result.getMovies().toString(), result.getHops(),
			// result.getOrginNode().getIP_address(),
			// result.getOrginNode().getPort_no());
			// UpdateTheCMD(output);

		} else if (Config.PING.equals(command)) {
			receivedMessages--;
			String host = tokenizer.nextToken();
			String hostport = tokenizer.nextToken();
			Node temp = new Node(host, Integer.parseInt(hostport));
			String pingMsg = Config.PINGOK + " " + Config.IP + " " + Config.PORT;
			sender(pingMsg, temp);
			sentMessages--;

		} else if (Config.PINGOK.equals(command)) {
			receivedMessages--;
			String host = tokenizer.nextToken();
			String hostport = tokenizer.nextToken();
			int port = Integer.parseInt(hostport);
			Node tempnode = neighbours.stream().filter(node -> port == node.getPort_no()).collect(Collectors.toList())
					.get(0);
			tempnode.setStatus("Active");
			String timeStamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date());
			tempnode.setUpdateTime(timeStamp);

		} else {
			unAnsweredMessages++;
		}

	}

	private void addNeighbour(Node node) {
		if (!neighbours.contains(node)) {
			neighbours.add(node);
		}
	}

	private void removeNeighbour(Node node) {
		for (Node neighbour : neighbours) {
			if (neighbour.getIP_address().equals(node.getIP_address())
					&& (neighbour.getPort_no() == node.getPort_no())) {
				neighbours.remove(neighbour);
				break;
			}
		}
	}

	public void clearSearchResults() {
		this.searchResultList = new ArrayList<SearchResult>();
	}

	public String printNeighbors() {
		String msg = "\n***********************\nNeighbous\n***********************\n";
		for (Node n : neighbours) {
			msg += n.getIP_address() + ": " + n.getPort_no() + "\n";
		}
		msg += "***********************\n";
		return msg;
	}

	public String printSearchResults() {
		String msg = "\n***********************\nSearch Results\n***********************\n";
		msg += "Origin" + "\t\t" + "Hops" + "\t" + "MovieCount" + "\t" + "Movies" + "\n";
		for (SearchResult a : searchResultList) {
			msg += a.getOrginNode().getIP_address() + ":" + a.getOrginNode().getPort_no() + "\t" + a.getHops() + "\t"
					+ a.getMoviesCount() + "\t" + a.getMovies().toString() + "\n";
		}
		msg += "***********************\n";
		return msg;
	}

	public List<Node> getNeighbours() {

		return neighbours;
	}

	public List<SearchResult> getSearchResults() {

		return searchResultList;
	}

	public String routingTable() {
		ArrayList<String> headers = new ArrayList<String>();
		headers.add("IPADDRESS");
		headers.add("PORT");
		headers.add("LASTUPDATE");
		headers.add("STATUS");

		ArrayList<ArrayList<String>> content = new ArrayList<ArrayList<String>>();
		for (Node node : neighbours) {
			ArrayList<String> row1 = new ArrayList<String>();
			row1.add(node.getIP_address());
			row1.add(Integer.toString(node.getPort_no()));
			try {
				String timeStamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date());
				SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				System.out.println(timeStamp);
				Date d1 = format.parse(node.getUpdateTime());
				Date d2 = format.parse(timeStamp);
				long diff = d2.getTime() - d1.getTime();
				long diffSeconds = diff / 1000 % 60;
				long diffMinutes = diff / (60 * 1000) % 60;
				long diffHours = diff / (60 * 60 * 1000) % 24;
				long diffDays = diff / (24 * 60 * 60 * 1000);
				row1.add(diffMinutes + " Min " + diffSeconds + " Sec");
			} catch (Exception e) {
				e.printStackTrace();
			}

			row1.add(node.getStatus());
			content.add(row1);
		}

		ConsoleTable ct = new ConsoleTable(headers, content);
		return "\n" + ct.printTable() + "\n";
	}

	public Statistics getStatistics() {
		Statistics stat = new Statistics();
		stat.setAnsweredMessages(receivedMessages - unAnsweredMessages);
		stat.setSentMessages(sentMessages);
		stat.setReceivedMessages(receivedMessages);
		stat.setNodeDegree(neighbours.size());
		if (latencyArray.size() > 0) {
			double avg = latencyArray.stream().mapToLong(val -> val).average().getAsDouble();
			stat.setLatencyMax(Collections.max(latencyArray));
			stat.setLatencyMin(Collections.min(latencyArray));
			stat.setLatencyAverage(avg);
			stat.setLatencySD(Utils.getStandardDeviation(latencyArray.toArray(), avg));
			stat.setNumberOfLatencies(latencyArray.size());

			avg = hopArray.stream().mapToLong(val -> val).average().getAsDouble();
			stat.setHopsMax(Collections.max(hopArray));
			stat.setHopsMin(Collections.min(hopArray));
			stat.setHopsAverage(avg);
			stat.setHopsSD(Utils.getStandardDeviation(hopArray.toArray(), avg));
			stat.setNumberOfHope(hopArray.size());

		}
		return stat;
	}

	public String printStatistics(Statistics stat) {
		String msg = "\n***********************\nStatistics\n***********************\n";
		msg += stat.toString();
		msg += "***********************\n";
		return msg;
	}

	public void clearStats() {
		receivedMessages = 0;
		sentMessages = 0;
		unAnsweredMessages = 0;
		latencyArray = new ArrayList<>();
		hopArray = new ArrayList<>();
	}

}
