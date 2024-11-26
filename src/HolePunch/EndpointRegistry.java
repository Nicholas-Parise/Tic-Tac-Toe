package HolePunch;

public class EndpointRegistry {

    /**
     * object to hold on to server IPs and PORTS
     * it also holds onto the amount of clients connected to this server
     * connectedClients is intended to used for load balancing
     */

    private int port;
    private String hostName;
    private int connectedClients;

    public EndpointRegistry(int port, String hostName){
        this.hostName = hostName;
        this.port = port;
        connectedClients = 0;
    }

    public int getPort() {
        return port;
    }

    public String getHostName() {
        return hostName;
    }

    public int getConnectedClients() {
        return connectedClients;
    }

    public void addConnectedClient(){
        connectedClients++;
    }


}
