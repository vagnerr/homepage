#!/usr/bin/perl -w
# ======================================================================
# Project:             Seti@Home
# Project Leader:      Peter Wise
# ----------------------------------------------------------------------
# Program name:        GetStat
# Program state:       pre-alpha
# Program notes:       Pull stats on a seti user using XML interface
#
# Program filename:    getstat.pl
# ----------------------------------------------------------------------
# Version  Author      Date       Comment
# ~~~~~~~~ ~~~~~~~~~~~ ~~~~~~~~~~ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# 1.0      P.J.Wise    10/02/2003 Initial version
#
# ----------------------------------------------------------------------
# CVS:
#    ID:        $Id: getstat.pl,v 1.1 2003/06/06 01:46:13 peter Exp $
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

my $TARGET_EMAIL = $ARGV[0] || 'yourname@your.domain';
my $TARGET_URL = "http://setiathome.ssl.berkeley.edu/fcgi-bin/fcgi?cmd=user_xml&email=$TARGET_EMAIL";

print "Getting data from server...";
if ( my $data = get($TARGET_URL)){
	print "Done\nProcessing...";
	my $ref_data = XMLin($data);
	print "Done\n\n";

	print "Number of Results: $ref_data->{userinfo}{numresults}\n";
	print "      Last Result: $ref_data->{userinfo}{lastresulttime}\n";
	print "             Rank: $ref_data->{rankinfo}{rank} / $ref_data->{rankinfo}{ranktotalusers}  (". (100-$ref_data->{rankinfo}{top_rankpct})."%)\n";
}else{
	print "Failed\n";
}
