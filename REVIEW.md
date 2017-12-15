# Feedback from peer review

## Peer review feedback:
- De leaderboardAdapter class is erg groot waardoor hij beter in een aparte java file kan. Dit maakt de algemene code overzichtelijker. 
- Comments boven elke file over wat deze file doet. Hierin moet ook de filename en de eigen naam. 
- Regel 58 in de mainActivity bevat een lege comment.
- Logout functie in home.java bevat geen comments.
- getQuestion in play.java bevat geen comments. 
- de klasses responseListener en errorListener maken de StringRequest call lastig te lezen omdat de algemene klasse ook zo heet, alleen dan met een hoofdletter zoals de java naming convention is. Geef deze klasses een andere naam die iets meer zegt over de functionaliteit.
- goToNextQuestion bevat maar een functiecall, dit kan ook direct. EDIT: nee dat kan niet omdat het een button onClickListener is.
- Het is een beetje apart om een term van een andere programmeertaal terug te zien in java terwijl je weet dat deze term niet bestaat in java (tuple). Imo is het dan juist duidelijk wat het doet.
- Als comments over meerdere regels gaan, is het beter om /**/ te gebruiken ipv //.