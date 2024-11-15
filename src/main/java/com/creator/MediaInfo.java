package com.creator;

import com.sun.jna.*;

import java.lang.reflect.Method;

import static java.util.Collections.singletonMap;

class MediaInfo {
    static String LibraryPath = "mediainfo";

    static {
        try {
            String os = System.getProperty("os.name");
            if (os != null && !os.toLowerCase().startsWith("windows") && !os.toLowerCase().startsWith("mac")) {
                final ClassLoader loader = MediaInfo.class.getClassLoader();
                final String LocalPath;
                if (loader != null) {
                    LocalPath = loader.getResource(MediaInfo.class.getName().replace('.', '/') + ".class").getPath().replace("MediaInfo.class", "");
                    try {
                        NativeLibrary.getInstance(LocalPath + "libzen.so.0");
                    } catch (LinkageError e) {
                        NativeLibrary.getInstance("zen");
                    }
                } else {
                    LocalPath = "";
                    NativeLibrary.getInstance("zen");
                }
                if (!LocalPath.isEmpty()) {
                    try {
                        NativeLibrary.getInstance(LocalPath + "libmediainfo.so.0");
                        LibraryPath = LocalPath + "libmediainfo.so.0";
                    } catch (LinkageError ignored) {
                    }
                }
            }
        } catch (LinkageError ignored) {
        }
    }

    private Pointer Handle;

    public MediaInfo() {
        Handle = MediaInfoDLL_Internal.INSTANCE.New();
    }

    public static String Option_Static(String Option) {
        return MediaInfoDLL_Internal.INSTANCE.Option(MediaInfoDLL_Internal.INSTANCE.New(), new WString(Option), new WString("")).toString();
    }

    public static String Option_Static(String Option, String Value) {
        return MediaInfoDLL_Internal.INSTANCE.Option(MediaInfoDLL_Internal.INSTANCE.New(), new WString(Option), new WString(Value)).toString();
    }

    public void dispose() {
        if (Handle == null)
            throw new IllegalStateException();

        MediaInfoDLL_Internal.INSTANCE.Delete(Handle);
        Handle = null;
    }

    @Override
    protected void finalize() throws Throwable {
        if (Handle != null)
            dispose();
    }

    public int Open(String File_Name) {
        return MediaInfoDLL_Internal.INSTANCE.Open(Handle, new WString(File_Name));
    }

    public int Open_Buffer_Init(long length, long offset) {
        return MediaInfoDLL_Internal.INSTANCE.Open_Buffer_Init(Handle, length, offset);
    }

    public int Open_Buffer_Continue(byte[] buffer, int size) {
        return MediaInfoDLL_Internal.INSTANCE.Open_Buffer_Continue(Handle, buffer, size);
    }

    public long Open_Buffer_Continue_GoTo_Get() {
        return MediaInfoDLL_Internal.INSTANCE.Open_Buffer_Continue_GoTo_Get(Handle);
    }

    public int Open_Buffer_Finalize() {
        return MediaInfoDLL_Internal.INSTANCE.Open_Buffer_Finalize(Handle);
    }

    public void Close() {
        MediaInfoDLL_Internal.INSTANCE.Close(Handle);
    }

    public String Inform() {
        return MediaInfoDLL_Internal.INSTANCE.Inform(Handle, 0).toString();
    }

    public String Get(StreamKind StreamKind, int StreamNumber, String parameter) {
        return Get(StreamKind, StreamNumber, parameter, InfoKind.Text, InfoKind.Name);
    }

    public String Get(StreamKind StreamKind, int StreamNumber, String parameter, InfoKind infoKind) {
        return Get(StreamKind, StreamNumber, parameter, infoKind, InfoKind.Name);
    }

    public String Get(StreamKind StreamKind, int StreamNumber, String parameter, InfoKind infoKind, InfoKind searchKind) {
        return MediaInfoDLL_Internal.INSTANCE.Get(Handle, StreamKind.ordinal(), StreamNumber, new WString(parameter), infoKind.ordinal(), searchKind.ordinal()).toString();
    }

    public String get(StreamKind StreamKind, int StreamNumber, int parameterIndex) {
        return Get(StreamKind, StreamNumber, parameterIndex, InfoKind.Text);
    }

    public String Get(StreamKind StreamKind, int StreamNumber, int parameterIndex, InfoKind infoKind) {
        return MediaInfoDLL_Internal.INSTANCE.GetI(Handle, StreamKind.ordinal(), StreamNumber, parameterIndex, infoKind.ordinal()).toString();
    }

    public int Count_Get(StreamKind StreamKind) {
        String StreamCount = Get(StreamKind, 0, "StreamCount");
        if (StreamCount == null || StreamCount.length() == 0)
            return 0;
        return Integer.parseInt(StreamCount);
    }

    public int Count_Get(StreamKind StreamKind, int StreamNumber) {
        return MediaInfoDLL_Internal.INSTANCE.Count_Get(Handle, StreamKind.ordinal(), StreamNumber);
    }

    public String Option(String Option) {
        return MediaInfoDLL_Internal.INSTANCE.Option(Handle, new WString(Option), new WString("")).toString();
    }

    public String Option(String Option, String Value) {
        return MediaInfoDLL_Internal.INSTANCE.Option(Handle, new WString(Option), new WString(Value)).toString();
    }

    public enum StreamKind {
        General,
        Video,
        Audio,
        Text,
        Other,
        Image,
        Menu
    }

    public enum InfoKind {
        Name,
        Text,
        Measure,
        Options,
        Name_Text,
        Measure_Text,
        Info,
        HowTo,
        Domain
    }

    public enum Status {
        None(0x00),
        Accepted(0x01),
        Filled(0x02),
        Updated(0x04),
        Finalized(0x08);

        private final int value;

        Status(int value) {
            this.value = value;
        }

        public int getValue(int value) {
            return value;
        }
    }

    interface MediaInfoDLL_Internal extends Library {
        MediaInfoDLL_Internal INSTANCE = Native.loadLibrary(LibraryPath, MediaInfoDLL_Internal.class, singletonMap(OPTION_FUNCTION_MAPPER, new FunctionMapper() {
                    @Override
                    public String getFunctionName(NativeLibrary lib, Method method) {
                        return "MediaInfo_" + method.getName();
                    }
                }
        ));

        Pointer New();

        void Delete(Pointer Handle);

        int Open(Pointer Handle, WString file);

        int Open_Buffer_Init(Pointer handle, long length, long offset);

        int Open_Buffer_Continue(Pointer handle, byte[] buffer, int size);

        long Open_Buffer_Continue_GoTo_Get(Pointer handle);

        int Open_Buffer_Finalize(Pointer handle);

        void Close(Pointer Handle);

        WString Inform(Pointer Handle, int Reserved);

        WString Get(Pointer Handle, int StreamKind, int StreamNumber, WString parameter, int infoKind, int searchKind);

        WString GetI(Pointer Handle, int StreamKind, int StreamNumber, int parameterIndex, int infoKind);

        int Count_Get(Pointer Handle, int StreamKind, int StreamNumber);

        WString Option(Pointer Handle, WString option, WString value);
    }
}