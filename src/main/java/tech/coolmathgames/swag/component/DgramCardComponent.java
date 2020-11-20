package tech.coolmathgames.swag.component;

import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractValue;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashSet;
import java.util.Set;

public class DgramCardComponent extends BaseComponent {
  public final Set<DgramSocket> connections = new HashSet<DgramSocket>();

  public DgramCardComponent(EnvironmentHost host) {
    super("swag_dgram", host);
    node = Network.newNode(this, Visibility.Network).withComponent(getComponentName()).withConnector(32).create();
  }

  @Callback(doc = "function():userdata -- Creates a Dgram socket")
  public Object[] open(Context context, Arguments arguments) throws IOException {
    if(connections.size() > 5) {
      throw new IOException("Too many Dgram sockets open!");
    }
    DgramSocket socket = new DgramSocket(this);
    connections.add(socket);
    return new Object[] { socket };
  }

  public class DgramSocket extends AbstractValue {
    private final DgramCardComponent component;
    private final DatagramChannel socket;

    public DgramSocket(DgramCardComponent component) throws IOException {
      this.socket = DatagramChannel.open(StandardProtocolFamily.INET);
      this.socket.bind(null);
      this.socket.configureBlocking(false);
      this.component = component;
    }

    @Callback(doc = "function():string, number, string -- Returns ip, port, and data of received packet")
    public Object[] read(Context context, Arguments arguments) throws IOException {
      ByteBuffer buffer = ByteBuffer.allocate(1024*1024);
      // No idea why this isn't already InetSocketAddress...
      InetSocketAddress socketAddress = (InetSocketAddress) socket.receive(buffer);
      if(socketAddress == null) {
        return null;
      }
      String received = new String(buffer.array(), 0, buffer.position());
      return new Object[] {socketAddress.getAddress(), socketAddress.getPort(), received};
    }

    @Callback(doc = "function(address:string, port:number, data:string) -- Sends packet to specified address")
    public Object[] write(Context context, Arguments arguments) throws IOException {
      byte[] buf = arguments.checkString(2).getBytes();
      this.socket.send(ByteBuffer.wrap(buf), new InetSocketAddress(arguments.checkString(0), arguments.checkInteger(1)));
      return null;
    }

    @Callback(doc = "function() -- Closes the socket")
    public Object[] close(Context context, Arguments arguments) {
      this.close();
      return null;
    }

    @Override
    public void dispose(Context context) {
      super.dispose(context);
      this.close();
    }

    private void close() {
      try {
        this.socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      component.connections.remove(this);
    }
  }
}
