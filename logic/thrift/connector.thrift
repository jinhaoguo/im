service ConnectorRpcService{
        bool online(string userId),
        void notify(string userId,string message)
    }
