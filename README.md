# CS4262 - Distributed System - Project Phase 3

## Distributed-Overlay-Network-WebService
Develop a simple overlay-based solution using web services that allows a set of nodes to share contents (e.g., music files) among each other.

## Run

Open CMD and go to your project folder 

run this command to build and run the project: mvn clean install tomcat7:run

API is available at http://localhost:8080/

## API calls

Register and join to the network : http://localhost:8080/register/?bootstrap_ip=BBOTSTRAP_IP&bootstrap_port=BOOTSTRAP_PORT&node_ip=NODE_IP&port=NODE_PORT&username=USERNAME

Leave the network and Unregister:
http://localhost:8080/leave

Show movies :
http://localhost:8080/movies

Search for a movie : 
http://localhost:8080/search/?query=QUERY

Show neighbors :
http://localhost:8080/neighbors

Show stats :
http://localhost:8080/stats

## Group Members
130001H A. H. K. T. Aberathne

130101N G. N. K. Dayarathna

130258B N. Kahagalle

130274U K. P. C. K. Karunanayake
