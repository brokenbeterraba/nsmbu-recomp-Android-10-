package android.media;
public class AudioAttributes {
 public static final int USAGE_MEDIA=1; public int usage;
 public static class Builder {private int usage; public Builder setUsage(int value){usage=value;return this;}
 public AudioAttributes build(){AudioAttributes out=new AudioAttributes();out.usage=usage;return out;}}
}
