package com.example.networkmodeswitcher;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/* JADX INFO: loaded from: classes3.dex */
public interface INetworkService extends IInterface {
    public static final String DESCRIPTOR = "com.example.networkmodeswitcher.INetworkService";

    void destroy() throws RemoteException;

    int getMode(int i) throws RemoteException;

    String setMode(int i, int i2, long j, int i3) throws RemoteException;

    public static class Default implements INetworkService {
        @Override // com.example.networkmodeswitcher.INetworkService
        public String setMode(int slotIndex, int subId, long mask, int legacyMode) throws RemoteException {
            return null;
        }

        @Override // com.example.networkmodeswitcher.INetworkService
        public int getMode(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.example.networkmodeswitcher.INetworkService
        public void destroy() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INetworkService {
        static final int TRANSACTION_destroy = 3;
        static final int TRANSACTION_getMode = 2;
        static final int TRANSACTION_setMode = 1;

        public Stub() {
            attachInterface(this, INetworkService.DESCRIPTOR);
        }

        public static INetworkService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(INetworkService.DESCRIPTOR);
            if (iin != null && (iin instanceof INetworkService)) {
                return (INetworkService) iin;
            }
            return new Proxy(obj);
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code >= 1 && code <= 16777215) {
                data.enforceInterface(INetworkService.DESCRIPTOR);
            }
            switch (code) {
                case 1598968902:
                    reply.writeString(INetworkService.DESCRIPTOR);
                    return true;
                default:
                    switch (code) {
                        case 1:
                            int _arg0 = data.readInt();
                            int _arg1 = data.readInt();
                            long _arg2 = data.readLong();
                            int _arg3 = data.readInt();
                            String _result = setMode(_arg0, _arg1, _arg2, _arg3);
                            reply.writeNoException();
                            reply.writeString(_result);
                            return true;
                        case 2:
                            int _arg4 = data.readInt();
                            int _result2 = getMode(_arg4);
                            reply.writeNoException();
                            reply.writeInt(_result2);
                            return true;
                        case 3:
                            destroy();
                            reply.writeNoException();
                            return true;
                        default:
                            return super.onTransact(code, data, reply, flags);
                    }
            }
        }

        private static class Proxy implements INetworkService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return INetworkService.DESCRIPTOR;
            }

            @Override // com.example.networkmodeswitcher.INetworkService
            public String setMode(int slotIndex, int subId, long mask, int legacyMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(INetworkService.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    _data.writeInt(subId);
                    _data.writeLong(mask);
                    _data.writeInt(legacyMode);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.example.networkmodeswitcher.INetworkService
            public int getMode(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(INetworkService.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.example.networkmodeswitcher.INetworkService
            public void destroy() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(INetworkService.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
