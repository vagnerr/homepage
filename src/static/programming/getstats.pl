#!/usr/bin/perl -w
# ======================================================================
# Project:             Seti@Home
# Project Leader:      Peter Wise
# ----------------------------------------------------------------------
# Program name:        GetStats
# Program state:       pre-alpha
# Program notes:       Pull stats on a seti user using XML interface
#
# Program filename:    getstats.pl
# ----------------------------------------------------------------------
# Version  Author      Date       Comment
# ~~~~~~~~ ~~~~~~~~~~~ ~~~~~~~~~~ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# 1.0      P.J.Wise    10/02/2003 Initial version
#
# ----------------------------------------------------------------------
# CVS:
#    ID:        $Id: getstats.pl,v 1.1 2003/06/06 01:46:13 peter Exp $
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

#Config

my $TARGET_NAME= $ARGV[0] || 'YourTeamName';
my $TARGET_URL = "http://setiathome.ssl.berkeley.edu/fcgi-bin/fcgi?cmd=team_lookup_xml&name=$TARGET_NAME";

$= = 20;
my ($nnn,$ccc,$rrr);
format STDOUT_TOP =
		Stats
Pos.  Name                             Results
-------------------------------------------------------------------
.
format STDOUT =
@>>   @<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  @>>>>>>>
$ccc, $nnn, $rrr
.



#print "Getting data from server...";
if ( my $data = get($TARGET_URL)){
	#print "Done\nProcessing...";

	my $ref_data = XMLin($data);
	#print "Done\n\n";
	my $members = $ref_data->{topmembers}{member};
	my $count = 0;
	foreach my $name (sort {$members->{$b}{numresults}<=>$members->{$a}{numresults}} keys %$members){
		$count++;
		$nnn = $name;
		$ccc = $count;
		$rrr = $members->{$name}{numresults};
		
		write();
		
	}
}else{
	print "Failed\n";
}
