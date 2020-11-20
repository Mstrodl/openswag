package tech.coolmathgames.swag.component;

import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.Visibility;

public class HumorCardComponent extends BaseComponent {
  public HumorCardComponent(EnvironmentHost host) {
    super("swag_humor", host);
    node = Network.newNode(this, Visibility.Network).withComponent(getComponentName()).withConnector(32).create();
  }

  @Callback(doc = "function():string; swag...")
  public Object[] swag(Context context, Arguments args) {
    return new Object[] {"Swag!"};
  }
}
