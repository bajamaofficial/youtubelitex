package id.nexora.youtubelitex;

public final class VideoItem {
    public final String id;
    public final String title;
    public final String channel;
    public final String thumbnail;

    public VideoItem(String id, String title, String channel, String thumbnail) {
        this.id = id;
        this.title = title;
        this.channel = channel;
        this.thumbnail = thumbnail;
    }
}
