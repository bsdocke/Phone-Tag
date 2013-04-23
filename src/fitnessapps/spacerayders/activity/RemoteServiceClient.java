package fitnessapps.spacerayders.activity;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.os.IBinder;
import android.os.RemoteException;
import fitnessapps.acceltest.activity.IAccelRemoteService;

public abstract class RemoteServiceClient extends BluetoothActivity {

 private RemoteServiceConnection conn;
 private IAccelRemoteService remoteService;

 public void gameStart() {
  if (isAccelServiceRunning()) { bindService(); }
 }

 /******************** Remote Service ***************************/
 private void bindService() {
  if (conn == null) {
   conn = new RemoteServiceConnection();
   Intent i = new Intent();
   i.setClassName("fitnessapps.acceltest.activity",
     "fitnessapps.acceltest.activity.AccelerometerService");
   bindService(i, conn, Context.BIND_AUTO_CREATE);
  }
 }

 void releaseService() {
  if (conn != null) {
   conn.serviceAppendEndGame();
   unbindService(conn);
   conn = null;
  }
 }

 
  private boolean isAccelServiceRunning() { 
   ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE); 
   for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) { 
    if ("fitnessapps.acceltest.activity.AccelerometerService".equals(service.service.getClassName())) { 
     return true;
    }
   } 
   return false; 
  }
  
 class RemoteServiceConnection implements ServiceConnection {
  public void onServiceConnected(ComponentName className,
    IBinder boundService) {
   remoteService = IAccelRemoteService.Stub
     .asInterface(boundService);
   try {
    remoteService.setGameNameFromService("Space Rayders");
   } catch (RemoteException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
   }
   //Log.d(getClass().getSimpleName(), "onServiceConnected()");
  }

  public void onServiceDisconnected(ComponentName className) {
   
   remoteService = null;
   //Log.d(getClass().getSimpleName(), "onServiceDisconnected");
  }
  
  public void serviceAppendEndGame() {
   try { 
     remoteService.setEndGameFlagFromService(true); 
     remoteService.setGameNameFromService("Space Rayders");
    } catch (RemoteException e) { 
     // TODO Auto-generated catch block
     e.printStackTrace(); 
    }
  }
 };

 /***************** End Remote Service ******************************/

 /**
  * Remember to unregister listeners when including this 
  onStop method in your activity class.
  *
  */
 @Override
public void onStop() {
 // unregisterListeners();
  releaseService();
  super.onStop();
 }

}