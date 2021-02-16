using Grpc.Core;
using System;
using System.Runtime.Serialization;

namespace ServerGStore {
    public enum ErrorMessage {
        [EnumMember(Value = "Object with Key {0} not Found!")]
        OBJ_NOT_FOUND,
        [EnumMember(Value = "Write Permission Denied on Partition {0}!")]
        W_DENIED
    }

    public class DomainException : Exception {
        public DomainException(ErrorMessage errorMessage, string arg) :
            base(string.Format(errorMessage.ToString(), arg)) { }
    }

    public class GRPCException : RpcException {
        public GRPCException(StatusCode statusCode, ErrorMessage errorMessage, string arg) :
            base(new Status(statusCode, string.Format(errorMessage.ToString(), arg))) { }

        public GRPCException(StatusCode statusCode, string msg) :
            base(new Status(statusCode, msg)) { }
    }
}
