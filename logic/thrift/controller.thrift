service ControllerRpcService{
    //status 0:offline,1:online
    void notifyConnectionStatus(1:string nodeName,2:string userId,3:i32 status),
	
	//auth interface
	i32 auth(1:string userId, 2:string userToken)
	
}
