package blockingblip;

import java.util.Scanner;
import java.io.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;

public class BlockingBlip {
	static HashMap<String, Player> lb;
	static ArrayList<Player> rank;
	static Scanner reader;
	static DecimalFormat df;
	static String defaultIGN;
	static String tempIGN;
//classes
	public static class Player {
		String ign;
		double hsp;
		int k;
		int hs;
		int w;
		int kw;
		
		public Player(String name, int kills, int headshots, int wins) {
			this.ign = name;
			this.k = kills;
			this.hs = headshots;
			this.w = wins;
			this.hsp = (double)headshots*100/kills;
			this.kw = (wins <= 0) ? kills : kills/wins;
		}
	}
	
	public static enum Color {
		reset("\u001B[0m"),
		defhex("\u001B[38;2;191;175;221m"),
		ignhex("\u001B[38;2;171;196;194m"),
		wordshex("\u001B[38;2;188;188;188m"),
		poshs("\u001B[38;2;143;206;0m"),
		neghs("\u001B[38;2;224;102;102m"),
		bold("\u001B[1m"),
		highlight("\u001B[93;1m"),
		underline("\033[4m");

	    private String code;

	    Color(String code) {
	        this.code = code;
	    }

	    public String getCode() {
	    	return code;
	    }
	}
//main
	public static void main(String[] args) throws Exception {
		startup();
		System.out.printf("%sWelcome to BlockingBlip!\n%s", Color.bold.getCode(), Color.reset.getCode());
		Thread.sleep(300);
		
		mainOptions();
		
		while (true) {
			Thread.sleep(10);
			
			System.out.printf("%sEnter Main Option ([v]iew options): ", Color.reset.getCode());
			String input = reader.nextLine().trim();
			
			if (input == "") extractStats(defaultIGN);
			else if (input.equals("v")) mainOptions();
			else if (input.equals("d") || input.equals("D")) setDefault();		
			else if (input.equals("l") || input.equals("L")) {
				lbOptions();
				leaderboard();
			}
			else extractStats(input);
		}
	}
//helpers	
	public static void setDefault() throws IOException {
		System.out.printf("%s\nSet default to: ", Color.reset.getCode());
		String dinput = reader.nextLine().trim();
		
		try { 
			String doc = Jsoup.connect("https://plancke.io/hypixel/player/stats/"+dinput+"#Arcade").get().text(); 
			defaultIGN = doc.substring(0, doc.indexOf("'"));

			FileWriter writer = new FileWriter("BBsettings.txt", false);
			writer.write(defaultIGN);
			writer.close();
	        
	        System.out.printf("%sdefault changed!\n\n%s", Color.wordshex.getCode(), Color.reset.getCode());
			}
		catch (IOException e) { System.out.printf("%sInvalid username!\n\n%s", Color.wordshex.getCode(), Color.reset.getCode()); }
	}

 	public static void extractStats(String ign) throws IOException, InterruptedException {
        try {
	        String doc = Jsoup.connect("https://plancke.io/hypixel/player/stats/"+ign+"#Arcade").get().text();
	        String ignSens = doc.substring(0,doc.indexOf("'"));

	        int k = Integer.parseInt(extractInt(doc, "Kills Blocking Dead: "));
	        int hs = Integer.parseInt(extractInt(doc, "Headshots Blocking Dead: "));
	        int w = Integer.parseInt(extractInt(doc, "Wins Blocking Dead: "));
	        double ohs = (double)hs*100/k;
	        int kw = k/w;
	        
	        if (ign.toLowerCase().equals(defaultIGN.toLowerCase())) {
	        	System.out.printf("\n%s%s%s%s \n", Color.bold.getCode(), Color.defhex.getCode(), ignSens, Color.reset.getCode());
	        }
	        else { System.out.printf("\n%s%s%s%s \n", Color.bold.getCode(), Color.ignhex.getCode(), ignSens, Color.reset.getCode()); }
     
        	Thread.sleep(10);
	        if (lb.containsKey(ignSens)) {
	        	Player p = new Player(ignSens, k, hs, w);
	        	
	        	rank.remove(lb.get(ignSens));
	        	rank.add(p);
	        	
	        	sortLB("k");
	        	System.out.printf("%sKills: %s%s%s %s[#%d]%s\n", Color.wordshex.getCode(), Color.reset.getCode(), Color.bold.getCode(), (String)df.format(k), Color.reset.getCode(), rank.indexOf(p)+1, Color.reset.getCode());
	        	Thread.sleep(10);
	        	sortLB("hs");
	        	System.out.printf("%sDings: %s%s%s %s[#%d]%s\n", Color.wordshex.getCode(), Color.reset.getCode(), Color.bold.getCode(), (String)df.format(hs), Color.reset.getCode(), rank.indexOf(p)+1, Color.reset.getCode());
	        	Thread.sleep(10);
	        	sortLB("w");
	        	System.out.printf("%sWins: %s%s%s %s[#%d]%s\n", Color.wordshex.getCode(), Color.reset.getCode(), Color.bold.getCode(), (String)df.format(w), Color.reset.getCode(), rank.indexOf(p)+1, Color.reset.getCode());
	        	Thread.sleep(10);
	        	sortLB("kw");
	        	System.out.printf("%sK/Wr: %s%s%s %s[#%d]%s\n", Color.wordshex.getCode(), Color.reset.getCode(), Color.bold.getCode(), (String)df.format(kw), Color.reset.getCode(), rank.indexOf(p)+1, Color.reset.getCode());
	        	Thread.sleep(10);
	        	sortLB("hsp");
	        	System.out.printf("%sOverall HS%%: %s%s%.3f%% %s[#%d]%s", Color.wordshex.getCode(), Color.reset.getCode(), Color.bold.getCode(), ohs, Color.reset.getCode(), rank.indexOf(p)+1, Color.reset.getCode());
	        	Thread.sleep(10);
	        	if (ohs>=lb.get(ignSens).hsp) System.out.printf(" %s%s+%.3f%s\n", Color.bold.getCode(), Color.poshs.getCode(), ohs-lb.get(ignSens).hsp, Color.reset.getCode());
	        	else System.out.printf(" %s%s%.3f%s\n", Color.bold.getCode(), Color.neghs.getCode(), ohs-lb.get(ignSens).hsp, Color.reset.getCode());
	        	Thread.sleep(10);
	        	System.out.printf("%sSession HS%%: %s%s%.3f%% %s(%d/%d)\n\n%s", Color.wordshex.getCode(), Color.reset.getCode(), Color.bold.getCode(), (double)(hs-lb.get(ignSens).hs)*100/(k-lb.get(ignSens).k), Color.reset.getCode(), (k-lb.get(ignSens).k)-(hs-lb.get(ignSens).hs), k-lb.get(ignSens).k, Color.reset.getCode());
	        	Thread.sleep(10);
	        	lb.put(ignSens, p);
	        }
	        else {
	        	System.out.printf("%sKills: %s%s%s\n%s", Color.wordshex.getCode(), Color.reset.getCode(), Color.bold.getCode(), (String)df.format(k), Color.reset.getCode());
	        	Thread.sleep(10);
	        	System.out.printf("%sDings: %s%s%s\n%s", Color.wordshex.getCode(), Color.reset.getCode(), Color.bold.getCode(), (String)df.format(hs), Color.reset.getCode());
	        	Thread.sleep(10);
	        	System.out.printf("%sHS%%: %s%s%.3f%%\n%s", Color.wordshex.getCode(), Color.reset.getCode(), Color.bold.getCode(), ohs, Color.reset.getCode());
	        	Thread.sleep(10);
	        	System.out.printf("%sWin: %s%s%s\n%s", Color.wordshex.getCode(), Color.reset.getCode(), Color.bold.getCode(), (String)df.format(w), Color.reset.getCode());
	        	Thread.sleep(10);
	        	System.out.printf("%sK/W: %s%s%s\n\n%s", Color.wordshex.getCode(), Color.reset.getCode(), Color.bold.getCode(), (String)df.format(kw), Color.reset.getCode());
	        	Thread.sleep(10);
	        }
        }
        catch (IOException e) { System.out.printf("%sInvalid Input: %s%s\n\n", Color.wordshex.getCode(), Color.reset.getCode(), ign); }
 	}

	public static String extractInt(String doc, String key) {
		int i = doc.indexOf(key)+key.length();
		int j = i+1;
		
		while (doc.charAt(j) != ' ') { j++; }
		return doc.substring(i,j).replaceAll(",", "");
	}

	public static String inLB(String s) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("LBdata.txt"));
		String line;
		while ((line = br.readLine())!= null) {
			String[] sep = line.split(" ");
			if (sep[0].toLowerCase().equals(s.toLowerCase())) {
				br.close();
				return sep[0];
			}
		}
		br.close();
		return "";
	}

	public static void sortLB(String s) {
		switch(s) {
		case "k":
        	Collections.sort(rank, Comparator.comparingInt(player->player.k));
        	Collections.reverse(rank);
        	break;
		case "w":
        	Collections.sort(rank, Comparator.comparingInt(player->player.w));
        	Collections.reverse(rank);
        	break;
		case "hsp":
        	Collections.sort(rank, Comparator.comparingDouble(player->player.hsp));
        	Collections.reverse(rank);
        	break;
		case "kw":
        	Collections.sort(rank, Comparator.comparingInt(player->player.kw));
        	Collections.reverse(rank);
        	break;
		case "hs":
        	Collections.sort(rank, Comparator.comparingInt(player->player.hs));
        	Collections.reverse(rank);
        	break;
		}
	}
	//actions	
	public static void leaderboard() throws Exception {
		Thread.sleep(10);
		System.out.printf("%sEnter LB Options ([v]iew options): ", Color.reset.getCode());
		String input = reader.nextLine().trim();

		switch (input.toLowerCase()) {
		case "v":
			lbOptions();
			leaderboard();
		case "1":
			sortLB("k");
        	lbSOptions();
        	leaderboardScroll(-1, 2);
        	break;
		case "2":
			sortLB("w");
        	lbSOptions();
        	leaderboardScroll(-1, 3);
        	break;
		case "3":
			sortLB("hsp");
        	lbSOptions();
        	leaderboardScroll(-1, 4);
        	break;
		case "4":
			sortLB("kw");
        	lbSOptions();
        	leaderboardScroll(-1, 5);
        	break;
		case "5":
			extractLB();
			leaderboard();
			break;
		case "q":
			System.out.println();
			return;
		default:
			Thread.sleep(10);
			String user = inLB(input);
			
			if (user == "") {
				System.out.printf("%sinvalid option or username!\n\n%s", Color.wordshex.getCode(), Color.reset.getCode());
				leaderboard();
			}
			
			tempIGN = user;
			System.out.printf("\n%sEnter LB Option for %s%s%s: ", Color.reset.getCode(), Color.highlight.getCode(), user, Color.reset.getCode());
			String uinput = reader.nextLine().trim();
			while (uinput.equals("v")) {
				lbOptions();
				Thread.sleep(10);
				System.out.printf("%sEnter LB Option for %s%s%s: ", Color.reset.getCode(), Color.highlight.getCode(), user, Color.reset.getCode());
				uinput = reader.nextLine().trim();
			}
        	switch (uinput) {
        	case "1":
            	sortLB("k");
            	lbSOptions();
            	leaderboardScroll(rank.indexOf(lb.get(user)), 2);
            	break;
        	case "2":
        		sortLB("w");
            	lbSOptions();
            	leaderboardScroll(rank.indexOf(lb.get(user)), 3);
            	break;
        	case "3":
        		sortLB("hsp");
            	lbSOptions();
            	leaderboardScroll(rank.indexOf(lb.get(user)), 4);
            	break;
        	case "4": 
        		sortLB("kw");
            	lbSOptions();
            	leaderboardScroll(rank.indexOf(lb.get(user)), 5);
            	break;
            default:
				System.out.printf("%sinvalid Filter!\n\n%s", Color.wordshex.getCode(), Color.reset.getCode());
				leaderboard();
				break;
        	}
			break;
		}
	}
	
	public static void leaderboardScroll(int a, int b) throws Exception {
		System.out.println();
		int s = Math.max(0, a-4);
		int e = Math.min(rank.size(), a+5);
		if (e == rank.size()) s = rank.size()-9;
		if (s == 0) e = s+9;
		int m = (s+e)/2;
		
		for (int i = s; i < e; i++) {
			System.out.printf("%d) ", i+1);
			switch (b) {
			case 2:
				System.out.printf("%s%s%s", Color.bold.getCode(), df.format(rank.get(i).k), Color.reset.getCode());
				break;
			case 3:
				System.out.printf("%s%s%s", Color.bold.getCode(), df.format(rank.get(i).w), Color.reset.getCode());
				break;
			case 4:
				System.out.printf("%s%.3f%%%s", Color.bold.getCode(), rank.get(i).hsp, Color.reset.getCode());
				break;
			case 5:
				System.out.printf("%s%s%s", Color.bold.getCode(), df.format(rank.get(i).kw), Color.reset.getCode());
				break;
			}
			if (rank.get(i).ign.equals(tempIGN)) System.out.printf(" - %s%s%s%s\n", Color.highlight.getCode(), Color.bold.getCode(), rank.get(i).ign, Color.reset.getCode());
			else System.out.println(" - " + rank.get(i).ign);
			Thread.sleep(10);
		}

		System.out.printf("\n%sEnter Scrolling Option ([v]iew options): ", Color.reset.getCode());
		String input = reader.nextLine().trim().toLowerCase();
		
		
		while (!input.equals("w") && !input.equals("a") && !input.equals("s") && !input.equals("d")) {
			if (input.equals("v")) {
				lbSOptions();
				Thread.sleep(10);
				System.out.printf("%sEnter Scrolling Option: ", Color.reset.getCode());
				input = reader.nextLine().trim();
			}
			else if (input.equals("q")) {
				tempIGN = "";
				System.out.println();
				leaderboard();	
				break;
			}
			else {
				System.out.printf("%sInvalid Scrolling Option!\n\n%s", Color.wordshex.getCode(), Color.reset.getCode());
				Thread.sleep(10);
				System.out.printf("%sEnter Scrolling Option: ", Color.reset.getCode());
				input = reader.nextLine().trim();
			}
		}
		
		switch (input) {
		case "w":
			leaderboardScroll(m-9, b);
			break;
		case "s":
			leaderboardScroll(m+9, b);
			break;
		case "a":
			leaderboardScroll(0, b);
			break;
		case "d":
			leaderboardScroll(1000, b);
			break;
		}
	}
//maintenance
	public static void extractLB() throws IOException {
		System.out.print("Updating...");
		
		System.setProperty("webdriver.chrome.driver", "C:\\Users\\blippy\\Documents\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");
		
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");
		WebDriver driver = new ChromeDriver(options);

		driver.get("https://plancke.io/hypixel/leaderboards/player.arcade.blocking_dead");
		
		try { Thread.sleep(1500); }
		catch (InterruptedException e) {
			System.out.println("Failed to connect! Trying Again...");
			driver.get("https://plancke.io/hypixel/leaderboards/player.arcade.blocking_dead");
			try { Thread.sleep(3000); }
			catch (InterruptedException ee) {
				System.out.print("Failed!");
				return;
			}
		}
		
		WebElement dropdown = driver.findElement(By.name("leaderboard-table_length"));
		Select select = new Select(dropdown);
		select.selectByValue("-1");
		
		try { Thread.sleep(750); }
		catch (InterruptedException e) {
			System.out.println("Failed to extract! Trying Again...");
			select.selectByValue("-1");
			try { Thread.sleep(1500); }
			catch (InterruptedException ee) {
				System.out.print("Failed!");
				return;
			}
		}
		
		String html = driver.getPageSource();	
		driver.quit();
		
		Document doc = Jsoup.parse(html);
		Elements rows = doc.select("tbody tr");
        FileWriter writer = new FileWriter("LBdata.txt", false);
        
        for (Element r : rows) {
	        String ignlink = r.select("td:eq(1) a").attr("href");
	        String ign = ignlink.substring(ignlink.lastIndexOf("/") + 1);
	        int k = Integer.parseInt(r.select("td:eq(2)").text().replace(",", ""));
	        int hs = Integer.parseInt(r.select("td:eq(3)").text().replace(",", ""));
	        int w = Integer.parseInt(r.select("td:eq(4)").text().replace(",", ""));
	        writer.write(ign + " " + k + " " + hs + " " + w + "\n");
        }

        writer.close();
        populateLB();
        System.out.println("Done!\n");
	}
	
	public static void populateLB() throws IOException {
		lb.clear();
		rank.clear();
        BufferedReader br = new BufferedReader(new FileReader("LBdata.txt"));
        String line;
        
        while ((line = br.readLine()) != null) {
        	String[] sep = line.split(" ");
        	String ref = sep[0];
        	if (!lb.containsKey(ref)) {
	        	Player n = new Player(ref, (int)Double.parseDouble(sep[1]), (int)Double.parseDouble(sep[2]), (int)Double.parseDouble(sep[3]));
	        	lb.put(sep[0],n);
	        	rank.add(n);
        	}
        }
        br.close();
	}
	
	public static void loadSettings() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("BBsettings.txt"));
        String line;
        
        while ((line = br.readLine()) != null) {
        	String[] sep = line.split(" ");
        	defaultIGN = sep[0];
        }
        br.close();
	}
	
	public static void startup() throws Exception {
		defaultIGN = "blippa";
		
		lb = new HashMap<>();
		rank = new ArrayList<>();	
		reader = new Scanner(System.in);
		df = new DecimalFormat("#,###");
		
		loadSettings();
		populateLB();
		extractLB();	
	}
//displays
	public static void lbSOptions() throws InterruptedException {
		System.out.println();
		Thread.sleep(10);
		System.out.printf("%s%sLeaderboard Scrolling:\n%s%s", Color.underline.getCode(), Color.wordshex.getCode(), Color.reset.getCode(), Color.wordshex.getCode());
		Thread.sleep(10);
		System.out.printf("- [w] Scroll forwards\n");
		Thread.sleep(10);
		System.out.printf("- [s] Scroll backwards\n");
		Thread.sleep(10);
		System.out.printf("- [a] Jump to beginning\n");
		Thread.sleep(10);
		System.out.printf("- [d] Jump to end\n");
		Thread.sleep(10);
		System.out.printf("- [q] Quit\n%s", Color.reset.getCode());
		Thread.sleep(10);
	}
	
	public static void mainOptions() throws InterruptedException {
		System.out.printf("\n%s%sMain Options:%s%s\n",Color.wordshex.getCode(), Color.underline.getCode(), Color.reset.getCode(), Color.wordshex.getCode());
		Thread.sleep(10);
		System.out.printf("- IGN (default=\"%s\")\n", defaultIGN);
		Thread.sleep(10);
		System.out.printf("- [D] Change default\n");
		Thread.sleep(10);
		System.out.printf("- [L] Leaderboard mode\n\n%s", Color.reset.getCode());
		Thread.sleep(10);
	}
	
	public static void lbOptions() throws InterruptedException {
		System.out.printf("\n%s%sLeaderboard Options:%s%s\n", Color.wordshex.getCode(), Color.underline.getCode(), Color.reset.getCode(), Color.wordshex.getCode());
		Thread.sleep(10);
		System.out.printf("- IGN lookup\n");
		Thread.sleep(10);
		System.out.printf("- [1] Kills\n");
		Thread.sleep(10);
		System.out.printf("- [2] Wins\n");
		Thread.sleep(10);
		System.out.printf("- [3] HS%%\n");
		Thread.sleep(10);
		System.out.printf("- [4] K/Wr\n");
		Thread.sleep(10);
		System.out.printf("- [5] Update LB\n");
		Thread.sleep(10);
		System.out.printf("- [q] Quit\n\n%s", Color.reset.getCode());
		Thread.sleep(10);
	}
}