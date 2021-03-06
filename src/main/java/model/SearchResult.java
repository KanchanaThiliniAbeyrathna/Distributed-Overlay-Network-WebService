package model;

import java.util.List;

/**
 * Created by Thilini on 11/5/2017.
 */

public class SearchResult {
	private Node orginNode;
    private List<String> movies;
    private int hops;
    private int moviesCount;
    private long timestamp;

    public SearchResult(Node orginNode, List<String> movies, int hops) {
        this.orginNode = orginNode;
        this.movies = movies;
        this.hops = hops;
    }

    public int getMoviesCount() {
		return moviesCount;
	}

	public void setMoviesCount(int moviesCount) {
		this.moviesCount = moviesCount;
	}

	public SearchResult() {
    }

    public Node getOrginNode() {
        return orginNode;
    }

    public void setOrginNode(Node orginNode) {
        this.orginNode = orginNode;
    }

    public List<String> getMovies() {
        return movies;
    }

    public void setMovies(List<String> movies) {
        this.movies = movies;
    }

    public int getHops() {
        return hops;
    }

    public void setHops(int hops) {
        this.hops = hops;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
