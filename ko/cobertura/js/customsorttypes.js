/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Olivier Parent
 *
 * Cobertura is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Cobertura is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cobertura; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */



function percentageSortType( s )
{
	var ret;
	var i = s.indexOf( "%" );

	if (i != -1) {
		s = s.substr( 0, i );
	}
	ret = parseFloat(s);
	if (isNaN(ret)) {
		ret = -1;
	}

	return ret;
}

SortableTable.prototype.addSortType( "Percentage", percentageSortType );



// This is needed for correctly sorting numbers in different
// locales.  The stock number converter only expects to sort
// numbers which use a period as a separator instead of a
// comma (like French).
function formattedNumberSortType( s )
{
	var ret;
	var i = s.indexOf(';');

	if (i != -1) {
		s = s.substring(0, i);
	}
	ret = parseFloat(s);
	if (isNaN(ret)) {
		return -1;
	}

	return ret;
}

SortableTable.prototype.addSortType( "FormattedNumber", formattedNumberSortType );
