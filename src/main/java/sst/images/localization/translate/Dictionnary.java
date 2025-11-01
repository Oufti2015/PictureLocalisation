package sst.images.localization.translate;

import sst.images.localization.file.FileCopier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Dictionnary {

    private static final Dictionnary me = new Dictionnary();
    private static final Map<String, String> dictionnary =  new HashMap<>();

    public static Dictionnary me() { return me; }

    private Dictionnary() {
    }

    public String translate(String key) {
        String translation = dictionnary.get(key);
        if (Objects.isNull(translation)) {
            translation = dictionnary.get(FileCopier.toCamelCase(key));
        }
        return !Objects.isNull(translation) ? translation : key;
    }

    static {
        dictionnary.put("جنوبسيناء", "Sud du Sinaï");
        dictionnary.put("شرمالشيخ", "Charm el-Cheikh");
        dictionnary.put("الجيزة", "Gizeh");
        dictionnary.put("ميدان الرماية", "Gizeh");
        dictionnary.put("أبو صير", "Abou Sir");
        dictionnary.put("القاهرة", "Caire");
        dictionnary.put("سقارة", "Saqqarah");
        dictionnary.put("الأقصر", "Louxor");
        dictionnary.put("نجعالخطبة", "Louxor");
        dictionnary.put("مدينهالقرنةالجديدة", "Nouvelle ville de Gourna");
        dictionnary.put("قريهالبعيرات", "Village d'Al-Ba'irat");
        dictionnary.put("الكرنكالقديم", "Vieux Karnak");
        dictionnary.put("قنا", "Qena");
        dictionnary.put("أسوان", "Assouan");
        dictionnary.put("صالح جاهين", "Assouan");
        dictionnary.put("مدينهادفو", "Madinat Hadfu");
        dictionnary.put("مدينهالسباعيةغرب", "Cité des Sept Ouest");
        dictionnary.put("نجعالسيدسعيدالجديد", "Nouveau Nagaa Al Sayed Saeed");
        dictionnary.put("الكاجوج", "Gebel Al-Silsila");
        dictionnary.put("مدينهكومامبو", "Kôm Ombo");
        dictionnary.put("جبلتقوق", "Jabal Taqouq");
        dictionnary.put("شريف سليمان", "Assouan");
        //dictionnary.put("صالحجاهين", "Assouan");
        dictionnary.put("مدينةأبوسمبل", "Abou Simbel");
        dictionnary.put("إسنا", "Esna");
        dictionnary.put("فارس", "Fars");
        dictionnary.put("مدينه دراو", "Médina de Drave");
        dictionnary.put("شاش", "Écran");
        dictionnary.put("Siou", "siou");
        dictionnary.put("Koti", "koti");
        dictionnary.put("نجع المحطة", "Assouan");
    }
}
