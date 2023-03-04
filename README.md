# PhotoUpdate
Készíts egy két layout-os, két “ablakos” Android alkamazást, ami egy telefon készüléken fut:
-	A fő layout-on a felhasználó beszkennel egy vonalkódot (barcode) 
-	Ugyanazon a layout-on készít egy fotót (megnyitja a kamera alkalmazást és a fotó rögzítése után behozza a képet az alkalmazásba), utána egy gombot megnyomva a felhasználó lekicsinyítve a beállított méretre feltölti egy FTP folderbe (lásd az elérési adatokat lennebb) a következő formában: yyyyMMdd alfolder, ezen belül a beolvasott vonalkód nevezetű folder, majd a fotó file neve “yyyyMMddhhmmss.jpg” formátumban:
 

-	Az app ablakban van egy alkalmazás logo (kis kép), amire 8x rákattintva megnyilik a layout a  beallításokkal (módosítható edittext): 
o	max szélesség illetve max magasság feltöltött fotók (maxX x maxY pixelbe bele kell férnie a kicsinyítés után)
o	FTP beállítások (user illetve jelszó)
o	Csak akkor lehet elmenteni a modosításokat, ha egy jelszó típusú mezőbe beírjuk az “Auto.ID” jelszót (ha nem a helyes jelszó van beírva, a mentés nem hajtódik végre)
o	A kimentett beállítások elmentődnek és a következő indulásnál is érvényben lesznek

Használható FTP kont:
 
User: 	tester@mikola.ro
Jelszo: 	Tester_2023!
