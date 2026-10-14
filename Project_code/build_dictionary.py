import csv, re

catalog_words = set()
with open('products.csv', encoding='utf-8') as f:
    r = csv.reader(f)
    header = next(r)
    for row in r:
        if not row: continue
        name, category = row[0], row[1]
        desc = row[3] if len(row) > 3 else ""
        text = name + " " + category + " " + desc
        for w in re.findall(r"[A-Za-z]+", text):
            w = w.lower()
            if len(w) >= 2:
                catalog_words.add(w)

colors = """red blue green yellow black white grey gray orange purple pink brown maroon navy beige
teal turquoise magenta violet indigo lavender crimson scarlet olive lime mint peach coral gold silver
bronze copper charcoal ivory cream tan khaki mustard rust burgundy plum mauve salmon aqua cyan
fuchsia amber emerald sapphire ruby onyx pearl slate cobalt denim wine rose lilac""".split()

materials = """cotton polyester leather suede nylon wool silk denim linen velvet canvas rubber
plastic metal steel aluminum wood bamboo glass ceramic stainless brass copper foam mesh fleece
spandex lycra cashmere satin chiffon corduroy tweed lace suede microfiber vinyl latex acrylic
carbon titanium alloy fiberglass plywood marble granite leatherette faux synthetic organic
recycled bioplastic terracotta porcelain stoneware""".split()

sizes = """small medium large extra tiny compact oversized regular slim wide narrow tall short
petite plus mini jumbo standard king queen twin full double single""".split()

adjectives = """lightweight durable stylish modern classic vintage elegant premium affordable
comfortable breathable waterproof wireless portable adjustable rechargeable washable stretchable
foldable stackable ergonomic reversible reflective insulated antibacterial hypoallergenic
scratch resistant shockproof anti slip quick drying fast charging noise cancelling energy
efficient eco friendly handmade branded imported original genuine premium budget luxury casual
formal sporty trendy chic rustic minimalist industrial bohemian coastal rugged sleek glossy
matte transparent opaque adjustable collapsible multipurpose heavy duty non stick leak proof""".split()

brands = """nike adidas puma reebok skechers converse woodland bata fila asics newbalance underarmour
samsung apple sony lg boat jbl sennheiser bose oneplus xiaomi redmi realme vivo oppo nokia
philips havells bajaj prestige bosch whirlpool haier godrej panasonic hitachi voltas blueStar
ikea urbanladder pepperfry nilkamal godrejinterio hp dell lenovo acer asus msi logitech canon
nikon fujifilm gopro fitbit garmin titan fastrack casio timex rolex fossil raymond allensolly
pantaloons westside levis wrangler pepejeans arrow vanheusen peterengland lakme maybelline
mac nivea dove loreal himalaya patanjali dabur colgate pepsodent parle britannia nestle amul
cadbury haldirams tata birla marico itc emami johnson gillette oral duracell eveready philips
prestige bajaj morphyrichards kent aquaguard eureka lg samsung mi honor tecno infinix""".split()

categories = """footwear electronics apparel clothing kitchen dining furniture beauty personal
care grocery toys games sports fitness stationery office home decor automotive baby pet supplies
books music instruments garden outdoor tools hardware health wellness luggage bags jewelry
watches mobile accessories computer bath bedding cleaning appliances lighting storage
gaming photography footcare skincare haircare fragrance snacks beverages dairy bakery""".split()

product_nouns = """shoes sneakers sandals slippers boots heels flats loafers flipflops chappals
shirt tshirt trousers jeans jacket sweater hoodie kurta saree dress skirt shorts leggings
blazer coat scarf gloves cap hat belt socks tie watch wallet bag backpack handbag purse
suitcase trolley phone charger cable earphones headphones speaker mouse keyboard monitor
laptop tablet camera drone router printer scanner fan mixer grinder blender toaster kettle
cooker oven microwave refrigerator washingmachine airconditioner heater iron vacuum cleaner
sofa chair table bed mattress pillow cushion blanket curtain rug carpet lamp mirror shelf
wardrobe cupboard drawer bottle flask mug plate bowl spoon fork knife pan pot cutlery
toothbrush toothpaste shampoo soap lotion cream perfume deodorant razor comb brush towel
bedsheet notebook pen pencil eraser sharpener stapler scissors folder marker crayon paint
brush bicycle helmet ball racket bat gym mat dumbbell treadmill yoga mattress stroller
diaper feeder toy puzzle doll robot car train kite balloon leash collar bowl cage aquarium
plant pot spade hose sprinkler ladder drill hammer wrench screwdriver nail bolt torch battery
bulb switch wire plug extension multiplier""".split()

misc_shopping = """cart checkout order delivery shipping discount offer coupon voucher warranty
return refund exchange invoice receipt payment wishlist rating review seller buyer customer
inventory stock catalog price category brand quantity availability trending bestseller
newarrival clearance sale festive combo bundle subscription membership loyalty rewards""".split()

extra = colors + materials + sizes + adjectives + brands + categories + product_nouns + misc_shopping
extra = [w.lower() for w in extra]

all_words = set(catalog_words)
all_words.update(extra)

all_words = sorted(w for w in all_words if w.isalpha() and len(w) >= 2)
print("total unique words:", len(all_words))

with open("dictionary.txt", "w", encoding="utf-8") as f:
    f.write("# SmartCart shopping dictionary - catalog vocabulary + shopping-domain terms\n")
    for w in all_words:
        f.write(w + "\n")
