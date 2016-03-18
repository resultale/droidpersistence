If you liked the project, click below and make the DroidPersistence grows increasingly

[![](https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=A65MDJVWKB7XY)



# This is a new library for doing persistence in SQLite Databases used in Android Applications. #

**Wow, the new version is ready droidpersistence** <br>
New Features:<br>
- Now you can determine a primary key of your table with annotation @PrimaryKey;<br>
- No need to inherit the class BaseModel with a feature ahead;<br>
- More dynamic data modeling;<br>


<br>
With DroidPersistence:<br>
- Yout dont't need to do a DDLs for tables, the library automatically make a create and drop table sintax.<br>
- Foreign keys enabled with "on delete cascade" and "on update cascade";<br>
- @nnotations  \o/;<br>
- creates an genereted value (id) for your models;<br>

<h2>You must create a model class:</h2>
<pre><code>
@Table(name="GAME")
public class Game {

	@PrimaryKey
	@Column(name="CODE")
	private long code;

	@Column(name="TITLE")
	private String title;
	
	@Column(name="PRODUCTION")	
	private String production;
	
	@Column(name="PLATFORM_ID")
	@ForeignKey(tableReference="PLATFORM", onDeleteCascade=true, columnReference="CODE")
	private long platform_id;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getProduction() {
		return production;
...
</code></pre>

<h2>A table definition:</h2>
<pre><code>public class GameTableDefinition extends TableDefinition&lt;Game&gt;{

	public GameTableDefinition() {
		super(Game.class);		
	}
}
</code></pre>

<h2>Finally, a Dao Class:</h2>
<pre><code>public class GameDao extends DroidDao&lt;Game,Long&gt; {

	public GameDao(TableDefinition&lt;Game&gt; tableDefinition, SQLiteDatabase database) {
		super(Game.class, tableDefinition, database);
	}
}
</code></pre>

<h2>Using Dao:</h2>
<pre><code>public class DataManager {

	private Context context;
	private SQLiteDatabase database;
	private GameDao gameDao;
	private PlatformDao platformDao;
	
	public DataManager(Context context){
		setContext(context);
		SQLiteOpenHelper openHelper = new OpenHelper(context, "GAMEDATABASE", null, 2);
		setDatabase(openHelper.getWritableDatabase());
		
		this.platformDao = new PlatformDao(new PlatformTableDefinition(), database);
		this.gameDao = new GameDao(new GameTableDefinition(), database);		
	}
...

	public Game getGame(Long id){		
		return getGameDao().get(id);
	}

	public List&lt;Game&gt; getGameList(){
		return getGameDao().getAll();
	}
	public long saveGame(Game game){
		long result = 0;		
		try {
			getDatabase().beginTransaction();
			result = getGameDao().save(game);
			getDatabase().setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		getDatabase().endTransaction();
		return result;
	}
...
</code></pre>

<h3>Save your code, use the quick and easy DroidPersistence.</h3>