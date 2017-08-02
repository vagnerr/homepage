#!/usr/bin/perl -w
# ======================================================================
# Project:             Seti@Home
# Project Leader:      Peter Wise
# ----------------------------------------------------------------------
# Program name:        StoreStats
# Program state:       pre-alpha
# Program notes:       Pull stats on a seti user using XML interface
#                      and store in database
#
# Program filename:    getstats.pl
# ----------------------------------------------------------------------
# Version  Author      Date       Comment
# ~~~~~~~~ ~~~~~~~~~~~ ~~~~~~~~~~ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# 1.0      P.J.Wise    16/5//2003 Initial version
#
# ----------------------------------------------------------------------
# CVS:
#    ID:        $Id: storestats.pl,v 1.1 2003/06/06 01:46:13 peter Exp $
#    Author:    $Author: peter $
#    Date:      $Date: 2003/06/06 01:46:13 $
#    Revision:  $Revision: 1.1 $
#
# ----------------------------------------------------------------------
# Notes:
# ~~~~~~
# 
# ======================================================================
use strict;
use LWP::Simple qw/get/;
use XML::Simple;
use Data::Dumper;
use DBI;
use Date::Parse qw(strptime);
use Date::Calc qw(Today);

#Config

my $TARGET_NAME= $ARGV[0] || 'YourTeamName';
my $TARGET_URL = "http://setiathome.ssl.berkeley.edu/fcgi-bin/fcgi?cmd=team_lookup_xml&name=$TARGET_NAME";

my $dbh = prepareDB();

if(notRunToday($dbh)){
	processToday($dbh);
}

$dbh->disconnect();


sub processToday{
	my($dbh) = shift();
	
	# pricess todays data (or at least try)
	if ( my $data = get($TARGET_URL)){

		my $ref_data = XMLin($data);
		my $members = $ref_data->{topmembers}{member};
		my $count = 0;
		foreach my $name (sort {$members->{$b}{numresults}<=>$members->{$a}{numresults}} keys %$members){
			$count++;
			my $Results = $members->{$name}{numresults};
			my $LastResult = getLastResult($members->{$name}{datelastresult});
			my $AverageCPU = avgToSeconds($members->{$name}{avecpu});
			my $TotalCPU = totalToSeconds($members->{$name}{totalcpu});
			my $User_ID = getUserID($dbh,$name,$members->{$name}{country});
			
			if($Results){#we dont want blank entries in the data table
				if ($LastResult ne getPreviousEntryDate($dbh,$User_ID)){
					#we have anew result so insert it
					insertData($dbh,$User_ID,$Results,$LastResult,
						   $AverageCPU,$TotalCPU);
				}
			}	
		}
		markRunToday($dbh);
	}else{
		#do nothing let cron run again in an hour
		#print "Failed\n";
	}
}

sub notRunToday{
	my($dbh) = shift();
	my $now = getToday();
	my $last = $dbh->selectrow_array('SELECT Value FROM CONFIG
					  WHERE Keyword=?',{},"LASTRUN");
	return($now ne $last);
}

sub markRunToday{
	my($dbh) = shift();
	my $now = getToday();
	$dbh->do('UPDATE CONFIG SET Value=? WHERE Keyword=?',{},$now,"LASTRUN");
}

sub getToday{
	my($y,$m,$d) = Today();
	my $return = sprintf("%4d%02d%02d",$y,$m,$d);
	return($return);
}

# settup the DB handle
sub prepareDB{
	my $dbh = DBI->connect("DBI:mysql:database=SETI","seti_u","seti_p",
				{RaiseError=>1,PrintError=>1, AutoCommit=>0});
	return $dbh;
}

# take the last result date string and comvert it to
# a MySQL compativle time (ie yyyy/mm/dd hh:mm:ss
sub getLastResult{
	my($result) = shift();
	if($result ne "Unknown"){
		my($sec,$min,$hour,$day,$month,$year) = strptime($result);
		$month++;
		$year +=1900;
		my $return = sprintf("%4d-%02d-%02d %02d:%02d:%02d",
					$year,$month,$day,$hour,$min,$sec);
		return($return);
	}else{
		return("");
	}
}

# take the time string from seti and return the value in seconds
# to 1 decimal place
sub avgToSeconds{
	my($average) = shift();
	if($average){
		my($hour,$junk1,$min,$junk2,$sec,$junk3) = split(' ',$average);
		return ( ($hour * 60 *60) + ($min * 60) + $sec);
	}else{
		return(0);
	}
}

#take the time string from seti and return the value it whole seconds
sub totalToSeconds{
	my($total) = shift();
	if($total =~ /years/){
		my($years,$junk) = split(' ',$total);
		return($years*31536000);
	}else{
		my ($hour,$junk1,$min,$junk) = split(' ',$total);
		return(($hour * 60 * 60) + ($min * 60));
	}
}

# get the user id from the database from the name and if it doesnt
# exist then add it
sub getUserID{
	my($dbh) = shift();
	my($name) = shift();
	my($country) = shift();
	my ($ID);
	if(!($ID = $dbh->selectrow_array('SELECT ID FROM USERS
					  WHERE NAME=?',{},$name))){
		#name doesnt exist so need to add
		my $countryID = getCountryID($dbh,$country);
		$dbh->do('INSERT INTO USERS(Name,Country_ID) VALUES(?,?)',
			 {},$name,$countryID);
		$ID = $dbh->{'mysql_insertid'};
	}
	return($ID);
}

sub getCountryID{
	my($dbh) = shift();
	my($country) = shift();
	my ($ID);
	if(!($ID = $dbh->selectrow_array('SELECT ID FROM COUNTRY
					  WHERE NAME=?',{},$country))){
		#name doesnt exist so need to add
		$dbh->do('INSERT INTO COUNTRY(Name) VALUES(?)',
			 {},$country);
		$ID = $dbh->{'mysql_insertid'};
	}
	return($ID);
}

#get the date of the last entry for the user
sub getPreviousEntryDate{
	my($dbh) = shift();
	my($User) = shift();
	my $date = $dbh->selectrow_array('SELECT LastResult FROM DATA
					  WHERE User_ID=?
					  ORDER BY LastResult DESC
					  LIMIT 0,1',{},$User);
	if($date){
		return($date);
	}else{
		return("");
	}
}

#insert the new data row
sub insertData{
	my($dbh) = shift();
	my($User_ID) = shift();
	my($Results) = shift();
	my($LastResult) = shift();
	my($AverageCPU) = shift();
	my($TotalCPU) = shift();

	$dbh->do('INSERT INTO DATA(User_ID,Results,LastResult,AverageCPU,
				   TotalCPU)
		  VALUES(?,?,?,?,?)',{},
		  $User_ID,$Results,$LastResult,$AverageCPU,$TotalCPU);
}




