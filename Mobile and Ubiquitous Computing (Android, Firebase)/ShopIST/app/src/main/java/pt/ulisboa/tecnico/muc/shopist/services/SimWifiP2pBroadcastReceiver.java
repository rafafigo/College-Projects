package pt.ulisboa.tecnico.muc.shopist.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;

public class SimWifiP2pBroadcastReceiver extends BroadcastReceiver {

  private final CommonViewModel commonViewModel;

  public SimWifiP2pBroadcastReceiver() {
    this.commonViewModel = CommonViewModel.getInstance();
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
      SimWifiP2pDeviceList deviceList =
          (SimWifiP2pDeviceList) intent.getSerializableExtra(SimWifiP2pBroadcast.EXTRA_DEVICE_LIST);
      String shoppingId = this.commonViewModel.getCart().getShoppingId();
      if (shoppingId == null) return;
      if (deviceList.getByName(this.commonViewModel.getShopping(shoppingId).getName()) != null) {
        new Thread(this.commonViewModel::checkIn).start();
      } else {
        new Thread(this.commonViewModel::checkOut).start();
      }
    }
  }
}
