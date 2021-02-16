using System;

namespace ClientGStore {
    class ClientException : Exception {
        public ClientException(string msg) :
            base(msg) { }
    }
}
