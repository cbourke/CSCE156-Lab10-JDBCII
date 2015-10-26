package unl.cse.albums;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Album {

	private Integer albumId;
	private String title;
	private Integer year;
	private Band band;
	private Integer albumNumber;
	private List<String> songTitles = new ArrayList<String>();
	
	public Album(Integer albumId, String title, Integer year, Band band,
			Integer albumNumber) {
		super();
		this.albumId = albumId;
		this.title = title;
		this.year = year;
		this.band = band;
		this.albumNumber = albumNumber;
	}

	public Album(String title, Integer year, String bandName) {
		this(null, title, year, new Band(bandName), null);
	}

	public Integer getAlbumId() {
		return albumId;
	}

	public String getTitle() {
		return title;
	}

	public Integer getYear() {
		return year;
	}

	public Integer getAlbumNumber() {
		return albumNumber;
	}

	public Band getBand() {
		return band;
	}

	public List<String> getSongTitles() {
		return songTitles;
	}

	public void addSong(String songTitle) {
		this.songTitles.add(songTitle);
	}
	
	/**
	 * This method returns a {@link #Album} instance loaded from the 
	 * database corresponding to the given <code>albumId</code>.  
	 * Throws an {@link IllegalStateException} upon an invalid <code>albumId</code>.
	 * All fields are loaded with this method.
	 * @param albumId
	 * @return
	 */
	public static Album getDetailedAlbum(int albumId) {

		Album a = null;
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException e) {
			System.out.println("InstantiationException: ");
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			System.out.println("IllegalAccessException: ");
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			System.out.println("ClassNotFoundException: ");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		Connection conn = null;

		try {
			conn = DriverManager.getConnection(DatabaseInfo.url, DatabaseInfo.username, DatabaseInfo.password);
		} catch (SQLException e) {
			System.out.println("SQLException: ");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		

		String query = "SELECT a.AlbumID    AS albumId, " +
		               "       a.AlbumTitle AS albumTitle, " +
				       "       a.AlbumYear  AS albumYear, " +
				       "       b.BandID     AS bandId, " +
				       "       a.AlbumNumber AS albumNumber " +
				       "FROM Albums a JOIN Bands b on a.BandID = b.BandID WHERE a.AlbumID = ?";
		
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, albumId);
			rs = ps.executeQuery();
			if(rs.next()) {
				String albumTitle = rs.getString("albumTitle");
				int albumYear     = rs.getInt("albumYear");
				int bandId        = rs.getInt("bandId");
				Band b = Band.getBand(bandId);
				int albumNumber   = rs.getInt("albumNumber");
				a = new Album(albumId, albumTitle, albumYear, b, albumNumber);
			} else {
				throw new IllegalStateException("No such album in database with id = " + albumId);
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			System.out.println("SQLException: ");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		//now get all the songs
		String songQuery = "SELECT s.SongTitle AS songTitle " +
				           "FROM AlbumSongs asong JOIN Songs s ON asong.SongID = s.SongID "+
				           "WHERE asong.AlbumID = ? ORDER BY asong.TrackNumber;";
		try {
			ps = conn.prepareStatement(songQuery);
			ps.setInt(1, albumId);
			rs = ps.executeQuery();
			while(rs.next()) {
				a.addSong(rs.getString("songTitle"));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			System.out.println("SQLException: ");
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		try {
			if(rs != null && !rs.isClosed())
				rs.close();
			if(ps != null && !ps.isClosed())
				ps.close();
			if(conn != null && !conn.isClosed())
				conn.close();
		} catch (SQLException e) {
			System.out.println("SQLException: ");
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		return a;
	}
	
	/**
	 * Returns a list of all albums in the database.  However, 
	 * only the title, year, and band name are loaded from the
	 * database.
	 * 
	 * @return
	 */
	public static List<Album> getAlbumSummaries() {
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException e) {
			System.out.println("InstantiationException: ");
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			System.out.println("IllegalAccessException: ");
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			System.out.println("ClassNotFoundException: ");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		Connection conn = null;

		try {
			conn = DriverManager.getConnection(DatabaseInfo.url, DatabaseInfo.username, DatabaseInfo.password);
		} catch (SQLException e) {
			System.out.println("SQLException: ");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		String query = "SELECT a.AlbumTitle AS albumTitle, " +
			           "       a.AlbumYear  AS albumYear, " +
			           "       b.BandName   AS bandName " +
			           "FROM Albums a LEFT JOIN Bands b on a.BandID = b.BandID";
		
		List<Album> albums = new ArrayList<Album>();

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while(rs.next()) {				
				String albumTitle = rs.getString("albumTitle");
				int albumYear     = rs.getInt("albumYear");
				String bandName   = rs.getString("bandName");
				Album a = new Album(albumTitle, albumYear, bandName);
				albums.add(a);
			}
		} catch (SQLException e) {
			System.out.println("SQLException: ");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		try {
			if(rs != null && !rs.isClosed())
				rs.close();
			if(ps != null && !ps.isClosed())
				ps.close();
			if(conn != null && !conn.isClosed())
				conn.close();
		} catch (SQLException e) {
			System.out.println("SQLException: ");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return albums;
	}

	@Override
	public String toString() {
		return "Album [albumId=" + albumId + ", title=" + title + ", year="
				+ year + ", band=" + band + ", albumNumber=" + albumNumber
				+ ", songTitles=" + songTitles + "]";
	}
	
	
	
	
}
