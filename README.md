# MusicPlayer

Create to Bullshit

# Sample code:  

```java
MusicPlayer musicPlayer = new MusicPlayer();
musicPlayer.open(new File("path"));
musicPlayer.play();

System.out.println(musicPlayer.getStatus());

//if played music extension is mp3, you can use code below
System.out.println(musicPlayer.getProperties("title"));
System.out.println(musicPlayer.getProperties("author"));
System.out.println(musicPlayer.getProperties("album"));
```

Reference:  
http://www.javazoom.net/mp3spi/documents.html
