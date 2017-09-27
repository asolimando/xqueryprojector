package fr.upsud.lri.pathExtractor;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.ParserConfigurationException;

import fr.upsud.lri.xqparser.ParseException;
import fr.upsud.lri.xqparser.SimpleNode;
import fr.upsud.lri.xqparser.XParser;
import junit.textui.ResultPrinter;

import org.junit.*;
import org.xml.sax.SAXException;
import org.tigris.subversion.javahl.*;

public class XMarkQuerySet {

	static ExtractedPaths exPaths = null;
	static ExtractedPaths expectedResult = null;
	static Paths stringReturned = null;
	static Paths stringUsed = null;
	static Paths nodeReturned = null;
	static Paths nodeUsed = null;
	static Paths everythingBelowUsed = null;
	static Path path = null;
	static String expr = "";
	static PrintWriter outFile = null;
	static int testNumber = 0;
	static long revNumber = -1;
	static boolean testSuccess = false;
	static String[] xMarksQuery = {
		"let $auction := doc(\"auction.xml\") " +
		"return for $b in $auction/site/people/person[@id = \"person0\"] " +
		"return $b/name/text()", 
		"let $auction := doc(\"auction.xml\") return " +
		" for $b in $auction/site/open_auctions/open_auction" +
		" return <increase>{$b/bidder[1]/increase/text()}</increase>", 
		"let $auction := doc(\"auction.xml\") " +
		" return for $b in $auction/site/open_auctions/open_auction " +
		" where zero-or-one($b/bidder[1]/increase/text()) * 2 " +
		" <= $b/bidder[last()]/increase/text() " +
		" return <increase " +
		"first=\"{$b/bidder[1]/increase/text()}\"   " +
		"last=\"{$b/bidder[last()]/increase/text()}\"" +
		"/>", 
		"let $auction := doc(\"auction.xml\") " +
		"return for $b in $auction/site/open_auctions/open_auction " +
		"where some $pr1 " +
		"in $b/bidder/personref[@person = \"person20\"]," +
		"$pr2 in $b/bidder/personref[@person = \"person51\"]" +
		"satisfies $pr1 << $pr2 " +
		"return <history>{$b/reserve/text()}</history>", 
		"let $auction := doc(\"auction.xml\") " +
		"return count(  for $i in $auction/site/closed_auctions/closed_auction  " +
		"where $i/price/text() >= 40 " +
		"return $i/price)",
		"let $auction := doc(\"auction.xml\") " +
		"return for $b in $auction//site/regions " +
		"return count($b//item)", 
		"let $auction := doc(\"auction.xml\")" +
		"return for $p in $auction/site " +
		"return  count($p//description) + count($p//annotation) + count($p//emailaddress)", 
		"let $auction := doc(\"auction.xml\") return" +
		" for $p in $auction/site/people/person" +
		" let $a :=" +
		" for $t in $auction/site/closed_auctions/closed_auction" +
		" where $t/buyer/@person = $p/@id" +
		"  return $t" +
		" return <item person=\"{$p/name/text()}\">{count($a)}</item>", 
		"let $auction := doc(\"auction.xml\") return" +
		" let $ca := $auction/site/closed_auctions/closed_auction return" +
		" let" +
		" $ei := $auction/site/regions/europe/item" +
		" for $p in $auction/site/people/person" +
		" let $a :=" +
		"  for $t in $ca" +
		"  where $p/@id = $t/buyer/@person" +
		"  return" +
		" let $n := for $t2 in $ei where $t/itemref/@item = $t2/@id return $t2" +
		" return <item>{$n/name/text()}</item>" +
		" return <person name=\"{$p/name/text()}\">{$a}</person>", 
		"let $auction := doc(\"auction.xml\") return" +
		" for $i in" +
		"  distinct-values($auction/site/people/person/profile/interest/@category)" +
		" let $p :=" +
		"  for $t in $auction/site/people/person" +
		"  where $t/profile/interest/@category = $i" +
		"  return" +
		"    <personne>" +
		"      <statistiques>" +
		"        <sexe>{$t/profile/gender/text()}</sexe>" +
		"        <age>{$t/profile/age/text()}</age>" +
		"        <education>{$t/profile/education/text()}</education>" +
		"        <revenu>{fn:data($t/profile/@income)}</revenu>" +
		"      </statistiques>" +
		"      <coordonnees>" +
		"        <nom>{$t/name/text()}</nom>" +
		"        <rue>{$t/address/street/text()}</rue>" +
		"        <ville>{$t/address/city/text()}</ville>" +
		"        <pays>{$t/address/country/text()}</pays>" +
		"        <reseau>" +
		"          <courrier>{$t/emailaddress/text()}</courrier>" +
		"          <pagePerso>{$t/homepage/text()}</pagePerso>" +
		"        </reseau>" +
		"      </coordonnees>" +
		"      <cartePaiement>{$t/creditcard/text()}</cartePaiement>" +
		"    </personne>" +
		" return <categorie>{<id>{$i}</id>, $p}</categorie>",
		"let $auction := doc(\"auction.xml\") return" +
		" for $p in $auction/site/people/person" +
		" let $l :=" +
		"  for $i in $auction/site/open_auctions/open_auction/initial" +
		"  where $p/profile/@income > 5000 * exactly-one($i/text())" +
		"  return $i" +
		" return <items name=\"{$p/name/text()}\">{count($l)}</items>", 
		"let $auction := doc(\"auction.xml\") return" +
		" for $p in $auction/site/people/person" +
		" let $l :=" +
		"  for $i in $auction/site/open_auctions/open_auction/initial" +
		"  where $p/profile/@income > 5000 * exactly-one($i/text())" +
		"  return $i" +
		" where $p/profile/@income > 50000" +
		" return <items person=\"{$p/profile/@income}\">{count($l)}</items>", 
		"let $auction := doc(\"auction.xml\") return" +
		" for $i in $auction/site/regions/australia/item" +
		" return <item name=\"{$i/name/text()}\">{$i/description}</item>", 
		"let $auction := doc(\"auction.xml\") return" +
		" for $i in $auction/site//item" +
		" where contains(string(exactly-one($i/description)), \"gold\")" +
				"return $i/name/text()", 
				"let $auction := doc(\"auction.xml\") return" +
				" for $a in" +
				"  $auction/site/closed_auctions/closed_auction/annotation/description/parlist/" +
				"   listitem/" +
				"   parlist/" +
				"   listitem/" +
				"   text/" +
				"   emph/" +
				"   keyword/" +
				"   text()" +
				" return <text>{$a}</text>",
				"let $auction := doc(\"auction.xml\") return" +
				" for $a in $auction/site/closed_auctions/closed_auction" +
				" where" +
				"  not(" +
				"    empty(" +
				"      $a/annotation/description/parlist/listitem/parlist/listitem/text/emph/" +
				"       keyword/" +
				"       text()" +
				"    )" +
				"  )" +
				" return <person id=\"{$a/seller/@person}\"/>", 
				"let $auction := doc(\"auction.xml\") return" +
				" for $p in $auction/site/people/person" +
				" where empty($p/homepage/text())" +
				" return <person name=\"{$p/name/text()}\"/>", 
				"declare namespace local = \"http://www.foobar.org\";" +
				" declare function local:convert($v as xs:decimal?) as xs:decimal?" +
				" {" +
				"  2.20371 * $v (: convert Dfl to Euro :)" +
				" };" +
				" let $auction := doc(\"auction.xml\") return" +
						" for $i in $auction/site/open_auctions/open_auction" +
						" return local:convert(zero-or-one($i/reserve))", 
						"let $auction := doc(\"auction.xml\") return" +
						" for $b in $auction/site/regions//item" +
						" let $k := $b/name/text()" +
						" order by zero-or-one($b/location) ascending empty greatest" +
						" return <item name=\"{$k}\">{$b/location/text()}</item>", 
						"let $auction := doc(\"auction.xml\") return" +
						" <result>" +
						"  <preferred>" +
						"    {count($auction/site/people/person/profile[@income >= 100000])}" +
						"  </preferred>" +
						"  <standard>" +
						"    {" +
						"      count(" +
						"        $auction/site/people/person/" +
						"         profile[@income < 100000 and @income >= 30000]" +
						"      )" +
						"    }" +
						"  </standard>" +
						"  <challenge>" +
						"    {count($auction/site/people/person/profile[@income < 30000])}" +
						"  </challenge>" +
						"  <na>" +
						"    {" +
						"      count(" +
						"        for $p in $auction/site/people/person" +
						"        where empty($p/profile/@income)" +
						"        return $p" +
						"      )" +
						"    }" +
						"  </na>" +
						" </result>"};
	
	@BeforeClass
	public static void init() throws ClientException{
		
		try {
			GregorianCalendar data = new GregorianCalendar();
			Date date = data.getTime();
			SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmss");
			outFile = new PrintWriter("C:/testProgettoLRI/xmarksOutput_" + simpleDate.format(date) + ".txt");
			SVNClient svnClient = new SVNClient();
			Status [] status = svnClient.status("C:/Users/Soliz/Documents/My Dropbox/workspace/paris-lri", true, false, true, true);
			for(Status stat : status)
				revNumber = (revNumber < stat.getRevisionNumber()) ? stat.getRevisionNumber() : revNumber;
			outFile.write("REVISION " + revNumber + "\n\n\n");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void cleaning(){
		
/*		outFile.write("\n\n" + result.getFailureCount() + " tests over " + result.getRunCount() + " failed\n");
		outFile.write("Total execution time: " + result.getRunTime() + "\n");
		outFile.write("Details of the failures:\n");
		for(Failure fail : result.getFailures()){
			outFile.write("\n\tdescription: " + fail.getDescription() + "\n");
			outFile.write("\tmessage: " + fail.getMessage() + "\n");
			outFile.write("\texception: " + fail.getException() + "\n");
		}*/
		outFile.close();
	}
	
	@Before
	public void prepareEnvForTest(){
		expectedResult = new ExtractedPaths(ExtractedPathsType.EP_QUERY);
		stringReturned = new Paths();
		stringUsed = new Paths();
		nodeReturned = new Paths();
		nodeUsed = new Paths();
		everythingBelowUsed = new Paths();
		path = null;
		
		expectedResult.put(PathType.STRING_RETURNED, stringReturned);
		expectedResult.put(PathType.STRING_USED, stringUsed);
		expectedResult.put(PathType.NODE_RETURNED, nodeReturned);
		expectedResult.put(PathType.NODE_USED, nodeUsed);
		expectedResult.put(PathType.EVERYTHING_BELOW_USED, everythingBelowUsed);
		
		outFile.write("-----------------START Test number: " + (testNumber+1) + "----------------" + "\n\n");
		outFile.write("+++++++QUERY Q" + (testNumber+1) + ":\n\n" + xMarksQuery[testNumber]);
	}
	
	@After
	public void endOfEachTest(){
		outFile.write("\n\n@@@@@@@@@@ TEST " + (testSuccess ? "PASSED" : "FAILED"));
		outFile.write("\n\n########## EXTRACTED PATHS START\n\n");
		outFile.write(exPaths.toString());
		outFile.write("\n########## EXTRACTED PATHS END\n");
		outFile.write("\n########## EXPECTED PATHS START\n\n");
		outFile.write(expectedResult.toString());
		outFile.write("\n########## EXPECTED PATHS END\n\n");
		outFile.write("-----------------END Test number: " + ++testNumber + "--------------" + "\n\n\n\n");
	}
	
	@Test
	public void test_XmarkQ1() 
	throws ParseException, ParserConfigurationException, SAXException, IOException {
		expr = "let $auction := doc(\"auction.xml\") " +
				"return for $b in $auction/site/people/person[@id = \"person0\"] " +
				"return $b/name/text()";
		exPaths = common(expr);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR b} / child::name / child::text()");
		stringReturned.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / attribute::id");
		everythingBelowUsed.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person");
		nodeUsed.add(path);

		testSuccess = expectedResult.equals(exPaths);
		assertTrue("Compare the expected ExtractedPaths with the computed one", testSuccess);
	}
	@Test
	public void test_XmarkQ2()
	throws ParseException, ParserConfigurationException, SAXException, IOException {
		expr = "let $auction := doc(\"auction.xml\") return " +
				" for $b in $auction/site/open_auctions/open_auction" +
				" return <increase>{$b/bidder[1]/increase/text()}</increase>";
		exPaths = common(expr);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction / {FOR b} / child::bidder / child::increase / child::text()");
		stringReturned.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction");
		nodeUsed.add(path);

		testSuccess = expectedResult.equals(exPaths);
		assertTrue("Compare the expected ExtractedPaths with the computed one", testSuccess);

	}
	@Test
	public void test_XmarkQ3()
	throws ParseException, ParserConfigurationException, SAXException, IOException {
		expr = "let $auction := doc(\"auction.xml\") " +
				" return for $b in $auction/site/open_auctions/open_auction " +
				" where zero-or-one($b/bidder[1]/increase/text()) * 2 " +
				" <= $b/bidder[last()]/increase/text() " +
				" return <increase " +
				"first=\"{$b/bidder[1]/increase/text()}\"   " +
				"last=\"{$b/bidder[last()]/increase/text()}\"" +
				"/>";
		exPaths = common(expr);

		//TODO: not sure, stub

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction / {FOR b} / child::bidder / child::increase / child::text()");
		stringUsed.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction / {FOR b} / child::bidder / child::increase / child::text()");
		stringReturned.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction / {FOR b} / child::bidder / child::increase / child::text()");
		stringReturned.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction / {FOR b} / child::bidder / self::node");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction / {FOR b} / child::bidder / self::node");
		nodeUsed.add(path);

		//path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction / {FOR b} / child::bidder / child::increase / child::text()");
		//everythingBelowUsed.add(path);

		testSuccess = expectedResult.equals(exPaths);
		assertTrue("Compare the expected ExtractedPaths with the computed one", testSuccess);

	}
	@Test//(expected=IllegalArgumentException.class)
	public void test_XmarkQ4() throws ParseException, ParserConfigurationException, SAXException, IOException {
		expr = "let $auction := doc(\"auction.xml\") " +
				"return for $b in $auction/site/open_auctions/open_auction " +
				"where some $pr1 " +
				"in $b/bidder/personref[@person = \"person20\"]," +
				"$pr2 in $b/bidder/personref[@person = \"person51\"]" +
				"satisfies $pr1 << $pr2 " +
				"return <history>{$b/reserve/text()}</history>";
		exPaths = common(expr);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction / {FOR b} / child::reserve / child::text()");
		stringReturned.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction / {FOR b} / child::bidder / child::personref / attribute::person");
		everythingBelowUsed.add(path);
		everythingBelowUsed.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction / {FOR b} / child::bidder / child::personref");
		nodeUsed.add(path);
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction / {FOR b} / child::bidder / child::personref / {QUANTIFIED pr1}");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction / {FOR b} / child::bidder / child::personref / {QUANTIFIED pr2}");
		nodeUsed.add(path);

		testSuccess = expectedResult.equals(exPaths);
		assertTrue("Compare the expected ExtractedPaths with the computed one", testSuccess);

	}
	@Test
	public void test_XmarkQ5() throws ParseException, ParserConfigurationException, SAXException, IOException {
		expr = "let $auction := doc(\"auction.xml\") " +
				"return count(  for $i in $auction/site/closed_auctions/closed_auction  " +
				"where $i/price/text() >= 40 " +
				"return $i/price)";
		exPaths = common(expr);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::closed_auctions / child::closed_auction / {FOR i} / child::price / child::text()");
		stringUsed.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::closed_auctions / child::closed_auction / {FOR i} / child::price");
		nodeUsed.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::closed_auctions / child::closed_auction");
		nodeUsed.add(path);

		testSuccess = expectedResult.equals(exPaths);
		assertTrue("Compare the expected ExtractedPaths with the computed one", testSuccess);

	}
	@Test
	public void test_XmarkQ6() throws ParseException, ParserConfigurationException, SAXException, IOException {
		expr = "let $auction := doc(\"auction.xml\") " +
				"return for $b in $auction//site/regions " +
				"return count($b//item)";
		exPaths = common(expr);

		path = new Path("doc(\"auction.xml\") / {LET auction} / descendant::site / child::regions / {FOR b} / descendant::item");
		nodeUsed.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / descendant::site / child::regions");
		nodeUsed.add(path);

		testSuccess = expectedResult.equals(exPaths);
		assertTrue("Compare the expected ExtractedPaths with the computed one", testSuccess);

	}
	@Test
	public void test_XmarkQ7() throws ParseException, ParserConfigurationException, SAXException, IOException {
		expr = "let $auction := doc(\"auction.xml\")" +
				"return for $p in $auction/site " +
				"return  count($p//description) + count($p//annotation) + count($p//emailaddress)";
		exPaths = common(expr);


		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / {FOR p} / descendant::description");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / {FOR p} / descendant::annotation");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / {FOR p} / descendant::emailaddress");
		nodeUsed.add(path);

		testSuccess = expectedResult.equals(exPaths);
		assertTrue("Compare the expected ExtractedPaths with the computed one", testSuccess);

	}
	@Test
	public void test_XmarkQ8() throws ParseException, ParserConfigurationException, SAXException, IOException {
		expr = "let $auction := doc(\"auction.xml\") return" +
				" for $p in $auction/site/people/person" +
				" let $a :=" +
				" for $t in $auction/site/closed_auctions/closed_auction" +
				" where $t/buyer/@person = $p/@id" +
				"  return $t" +
				" return <item person=\"{$p/name/text()}\">{count($a)}</item>";
		exPaths = common(expr);


		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::closed_auctions / child::closed_auction / {FOR t} / child::buyer / attribute::person");
		everythingBelowUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR p} / attribute::id");
		everythingBelowUsed.add(path);


		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR p} / child::name / child::text()");
		stringReturned.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::closed_auctions / child::closed_auction");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::closed_auctions / child::closed_auction / {FOR t} / {LET a}");
		nodeUsed.add(path);

		testSuccess = expectedResult.equals(exPaths);
		assertTrue("Compare the expected ExtractedPaths with the computed one", testSuccess);

	}
	@Test
	public void test_XmarkQ9() throws ParseException, ParserConfigurationException, SAXException, IOException {
		expr = "let $auction := doc(\"auction.xml\") return" +
				" let $ca := $auction/site/closed_auctions/closed_auction return" +
				" let" +
				" $ei := $auction/site/regions/europe/item" +
				" for $p in $auction/site/people/person" +
				" let $a :=" +
				"  for $t in $ca" +
				"  where $p/@id = $t/buyer/@person" +
				"  return" +
				" let $n := for $t2 in $ei where $t/itemref/@item = $t2/@id return $t2" +
				" return <item>{$n/name/text()}</item>" +
				" return <person name=\"{$p/name/text()}\">{$a}</person>";

		exPaths = common(expr);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR p} / attribute::id");
		everythingBelowUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::closed_auctions / child::closed_auction / {LET ca} / {FOR t} / child::buyer / attribute::person");
		everythingBelowUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::closed_auctions / child::closed_auction / {LET ca} / {FOR t} / child::itemref / attribute::item");
		everythingBelowUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::regions / child::europe / child::item / {LET ei} / {FOR t2} / attribute::id");
		everythingBelowUsed.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR p} / child::name / child::text()");
		stringReturned.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::regions / child::europe / child::item / {LET ei} / {FOR t2} / {LET n} / child::name / child::text() / {LET a}");
		stringReturned.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::closed_auctions / child::closed_auction / {LET ca}");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::regions / child::europe / child::item / {LET ei}");
		nodeUsed.add(path);

		testSuccess = expectedResult.equals(exPaths);
		assertTrue("Compare the expected ExtractedPaths with the computed one", testSuccess);

	}
	@Test
	public void test_XmarkQ10() throws ParseException, ParserConfigurationException, SAXException, IOException {
		expr = "let $auction := doc(\"auction.xml\") return" +
				" for $i in" +
				"  distinct-values($auction/site/people/person/profile/interest/@category)" +
				" let $p :=" +
				"  for $t in $auction/site/people/person" +
				"  where $t/profile/interest/@category = $i" +
				"  return" +
				"    <personne>" +
				"      <statistiques>" +
				"        <sexe>{$t/profile/gender/text()}</sexe>" +
				"        <age>{$t/profile/age/text()}</age>" +
				"        <education>{$t/profile/education/text()}</education>" +
				"        <revenu>{fn:data($t/profile/@income)}</revenu>" +
				"      </statistiques>" +
				"      <coordonnees>" +
				"        <nom>{$t/name/text()}</nom>" +
				"        <rue>{$t/address/street/text()}</rue>" +
				"        <ville>{$t/address/city/text()}</ville>" +
				"        <pays>{$t/address/country/text()}</pays>" +
				"        <reseau>" +
				"          <courrier>{$t/emailaddress/text()}</courrier>" +
				"          <pagePerso>{$t/homepage/text()}</pagePerso>" +
				"        </reseau>" +
				"      </coordonnees>" +
				"      <cartePaiement>{$t/creditcard/text()}</cartePaiement>" +
				"    </personne>" +
				" return <categorie>{<id>{$i}</id>, $p}</categorie>";

		exPaths = common(expr);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR t} / child::profile / child::interest / attribute::category");
		everythingBelowUsed.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR t} / child::profile / child::gender / child::text() / {LET p}");
		stringReturned.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR t} / child::profile / child::age / child::text() / {LET p}");
		stringReturned.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR t} / child::profile / child::education / child::text() / {LET p}");
		stringReturned.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR t} / child::name / child::text() / {LET p}");
		stringReturned.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR t} / child::address / child::street / child::text() / {LET p}");
		stringReturned.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR t} / child::address / child::city / child::text() / {LET p}");
		stringReturned.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR t} / child::address / child::country / child::text() / {LET p}");
		stringReturned.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR t} / child::emailaddress / child::text() / {LET p}");
		stringReturned.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR t} / child::homepage / child::text() / {LET p}");
		stringReturned.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR t} / child::creditcard / child::text() / {LET p}");
		stringReturned.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / child::profile / child::interest / attribute::category");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR t} / child::profile / attribute::income");
		nodeUsed.add(path);

		testSuccess = expectedResult.equals(exPaths);
		assertTrue("Compare the expected ExtractedPaths with the computed one", testSuccess);

	}
	@Test
	public void test_XmarkQ11() throws ParseException, ParserConfigurationException, SAXException, IOException {
		expr = "let $auction := doc(\"auction.xml\") return" +
				" for $p in $auction/site/people/person" +
				" let $l :=" +
				"  for $i in $auction/site/open_auctions/open_auction/initial" +
				"  where $p/profile/@income > 5000 * exactly-one($i/text())" +
				"  return $i" +
				" return <items name=\"{$p/name/text()}\">{count($l)}</items>";

		exPaths = common(expr);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR p} / child::name / child::text()");
		stringReturned.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction / child::initial / {FOR i} / child::text()");
		stringUsed.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR p} / child::profile / attribute::income");
		everythingBelowUsed.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction / child::initial / {FOR i} / {LET l}");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction / child::initial");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person");
		nodeUsed.add(path);
		//path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR p} / child::profile / attribute::income");
		//nodeUsed.add(path); //TODO: to see, is in ebu so...

		testSuccess = expectedResult.equals(exPaths);
		assertTrue("Compare the expected ExtractedPaths with the computed one", testSuccess);

	}
	@Test
	public void test_XmarkQ12() throws ParseException, ParserConfigurationException, SAXException, IOException {
		expr = "let $auction := doc(\"auction.xml\") return" +
				" for $p in $auction/site/people/person" +
				" let $l :=" +
				"  for $i in $auction/site/open_auctions/open_auction/initial" +
				"  where $p/profile/@income > 5000 * exactly-one($i/text())" +
				"  return $i" +
				" where $p/profile/@income > 50000" +
				" return <items person=\"{$p/profile/@income}\">{count($l)}</items>";

		exPaths = common(expr);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction / child::initial / {FOR i} / child::text()");
		stringUsed.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR p} / child::profile / attribute::income");
		everythingBelowUsed.add(path);
		everythingBelowUsed.add(path); // TODO: to remove when we will clean duplicates, now is ok

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction / child::initial");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction / child::initial / {FOR i} / {LET l}");
		nodeUsed.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR p} / child::profile / attribute::income");
		nodeReturned.add(path);

		testSuccess = expectedResult.equals(exPaths);
		assertTrue("Compare the expected ExtractedPaths with the computed one", testSuccess);

	}
	@Test
	public void test_XmarkQ13() throws ParseException, ParserConfigurationException, SAXException, IOException {
		expr = "let $auction := doc(\"auction.xml\") return" +
				" for $i in $auction/site/regions/australia/item" +
				" return <item name=\"{$i/name/text()}\">{$i/description}</item>";

		exPaths = common(expr);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::regions / child::australia / child::item / {FOR i} / child::name / child::text()");
		stringReturned.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::regions / child::australia / child::item");
		nodeUsed.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::regions / child::australia / child::item / {FOR i} / child::description");
		nodeReturned.add(path);

		testSuccess = expectedResult.equals(exPaths);
		assertTrue("Compare the expected ExtractedPaths with the computed one", testSuccess);

	}
	@Test
	public void test_XmarkQ14() throws ParseException, ParserConfigurationException, SAXException, IOException {
		expr = "let $auction := doc(\"auction.xml\") return" +
				" for $i in $auction/site//item" +
				" where contains(string(exactly-one($i/description)), \"gold\")" +
						"return $i/name/text()";

		exPaths = common(expr);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / descendant::item / {FOR i} / child::name / child::text()");
		stringReturned.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / descendant::item");
		nodeUsed.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / descendant::item / {FOR i} / child::description");
		nodeUsed.add(path);

		testSuccess = expectedResult.equals(exPaths);
		assertTrue("Compare the expected ExtractedPaths with the computed one", testSuccess);

	}
	@Test
	public void test_XmarkQ15() throws ParseException, ParserConfigurationException, SAXException, IOException {
		expr = "let $auction := doc(\"auction.xml\") return" +
				" for $a in" +
				"  $auction/site/closed_auctions/closed_auction/annotation/description/parlist/" +
				"   listitem/" +
				"   parlist/" +
				"   listitem/" +
				"   text/" +
				"   emph/" +
				"   keyword/" +
				"   text()" +
				" return <text>{$a}</text>";

		exPaths = common(expr);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::closed_auctions / child::closed_auction / child::annotation / child::description / child::parlist / child::listitem / child::parlist / child::listitem / child::text / child::emph / child::keyword / child::text()");
		stringUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::closed_auctions / child::closed_auction / child::annotation / child::description / child::parlist / child::listitem / child::parlist / child::listitem / child::text / child::emph / child::keyword / child::text() / {FOR a}");
		stringReturned.add(path);

		testSuccess = expectedResult.equals(exPaths);
		assertTrue("Compare the expected ExtractedPaths with the computed one", testSuccess);

	}
	@Test
	public void test_XmarkQ16() throws ParseException, ParserConfigurationException, SAXException, IOException {
		expr = "let $auction := doc(\"auction.xml\") return" +
				" for $a in $auction/site/closed_auctions/closed_auction" +
				" where" +
				"  not(" +
				"    empty(" +
				"      $a/annotation/description/parlist/listitem/parlist/listitem/text/emph/" +
				"       keyword/" +
				"       text()" +
				"    )" +
				"  )" +
				" return <person id=\"{$a/seller/@person}\"/>";

		exPaths = common(expr);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::closed_auctions / child::closed_auction / {FOR a} / child::seller / attribute::person");
		nodeReturned.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::closed_auctions / child::closed_auction / {FOR a} / child::annotation / child::description / child::parlist / child::listitem / child::parlist / child::listitem / child::text / child::emph / child::keyword / child::text()");
		stringUsed.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::closed_auctions / child::closed_auction");
		nodeUsed.add(path);

		testSuccess = expectedResult.equals(exPaths);
		assertTrue("Compare the expected ExtractedPaths with the computed one", testSuccess);

	}
	@Test
	public void test_XmarkQ17() throws ParseException, ParserConfigurationException, SAXException, IOException {
		expr = "let $auction := doc(\"auction.xml\") return" +
				" for $p in $auction/site/people/person" +
				" where empty($p/homepage/text())" +
				" return <person name=\"{$p/name/text()}\"/>";

		exPaths = common(expr);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR p} / child::name / child::text()");
		stringReturned.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR p} / child::homepage / child::text()");
		stringUsed.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person");
		nodeUsed.add(path);

		testSuccess = expectedResult.equals(exPaths);
		assertTrue("Compare the expected ExtractedPaths with the computed one", testSuccess);

	}
	@Test
	public void test_XmarkQ18() throws ParseException, ParserConfigurationException, SAXException, IOException {
		expr = "declare namespace local = \"http://www.foobar.org\";" +
				" declare function local:convert($v as xs:decimal?) as xs:decimal?" +
				" {" +
				"  2.20371 * $v (: convert Dfl to Euro :)" +
				" };" +
				" let $auction := doc(\"auction.xml\") return" +
						" for $i in $auction/site/open_auctions/open_auction" +
						" return local:convert(zero-or-one($i/reserve))";

		exPaths = common(expr);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::open_auctions / child::open_auction / {FOR i} / child::reserve");
		nodeUsed.add(path);

		testSuccess = expectedResult.equals(exPaths);
		assertTrue("Compare the expected ExtractedPaths with the computed one", testSuccess);

	}
	@Test
	public void test_XmarkQ19() throws ParseException, ParserConfigurationException, SAXException, IOException {
		expr = "let $auction := doc(\"auction.xml\") return" +
				" for $b in $auction/site/regions//item" +
				" let $k := $b/name/text()" +
				" order by zero-or-one($b/location) ascending empty greatest" +
				" return <item name=\"{$k}\">{$b/location/text()}</item>";

		exPaths = common(expr);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::regions / descendant::item / {FOR b} / child::location / child::text()");
		stringReturned.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::regions / descendant::item / {FOR b} / child::name / child::text() / {LET k}");
		stringReturned.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::regions / descendant::item");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::regions / descendant::item / {FOR b} / child::location");
		nodeUsed.add(path);

		testSuccess = expectedResult.equals(exPaths);
		assertTrue("Compare the expected ExtractedPaths with the computed one", testSuccess);

	}
	@Test
	public void test_XmarkQ20() throws ParseException, ParserConfigurationException, SAXException, IOException {
		expr = "let $auction := doc(\"auction.xml\") return" +
				" <result>" +
				"  <preferred>" +
				"    {count($auction/site/people/person/profile[@income >= 100000])}" +
				"  </preferred>" +
				"  <standard>" +
				"    {" +
				"      count(" +
				"        $auction/site/people/person/" +
				"         profile[@income < 100000 and @income >= 30000]" +
				"      )" +
				"    }" +
				"  </standard>" +
				"  <challenge>" +
				"    {count($auction/site/people/person/profile[@income < 30000])}" +
				"  </challenge>" +
				"  <na>" +
				"    {" +
				"      count(" +
				"        for $p in $auction/site/people/person" +
				"        where empty($p/profile/@income)" +
				"        return $p" +
				"      )" +
				"    }" +
				"  </na>" +
				" </result>";

		exPaths = common(expr);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / child::profile / attribute::income");
		everythingBelowUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / child::profile / attribute::income");
		everythingBelowUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / child::profile / attribute::income");
		everythingBelowUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / child::profile / attribute::income");
		everythingBelowUsed.add(path);

		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / child::profile");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / child::profile");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / child::profile");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR p} / child::profile / attribute::income");
		nodeUsed.add(path);
		path = new Path("doc(\"auction.xml\") / {LET auction} / child::site / child::people / child::person / {FOR p}");
		nodeUsed.add(path);

		testSuccess = expectedResult.equals(exPaths);
		assertTrue("Compare the expected ExtractedPaths with the computed one", testSuccess);

	}

	private ExtractedPaths common(String query)
	throws ParseException, ParserConfigurationException, SAXException, IOException{
		XParser parser = new XParser(
				new java.io.StringBufferInputStream(query));
		SimpleNode root = parser.START();
		QueryPathExtractor queryPathExtractor = new QueryPathExtractor();
		return queryPathExtractor.extractPaths(
				root, new Environment(), null, new Path((UpdateOperationType) null), false, null);
	}
}

