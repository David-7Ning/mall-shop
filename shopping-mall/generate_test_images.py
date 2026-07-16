from PIL import Image, ImageDraw, ImageFont
import random
import os

# 商品名称列表
products = [
    "iPhone 15 Pro",
    "华为Mate 60 Pro", 
    "小米14",
    "MacBook Pro 14",
    "联想ThinkPad X1",
    "戴尔U2723QE",
    "海尔冰箱 BCD-470",
    "美的空调 KFR-72LW"
]

output_dir = "/tmp/real-images"
os.makedirs(output_dir, exist_ok=True)

for i, product in enumerate(products, 1):
    # 创建 300x300 的彩色图片
    img = Image.new('RGB', (300, 300), color=(
        random.randint(100, 255),
        random.randint(100, 255), 
        random.randint(100, 255)
    ))
    
    draw = ImageDraw.Draw(img)
    
    # 添加商品名称文字
    try:
        font = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 24)
    except:
        font = ImageFont.load_default()
    
    # 文字居中
    bbox = draw.textbbox((0, 0), product, font=font)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]
    x = (300 - text_width) / 2
    y = (300 - text_height) / 2
    
    draw.text((x, y), product, fill=(255, 255, 255), font=font)
    
    # 保存图片
    output_path = os.path.join(output_dir, f"product-{i}.png")
    img.save(output_path, 'PNG')
    print(f"生成图片: {output_path}")

print("所有测试图片生成完成！")