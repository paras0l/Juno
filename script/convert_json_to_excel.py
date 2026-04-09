import json
import pandas as pd
import re

# 读取 JSON 文件
def read_json_file(file_path):
    """读取 JSON 文件，每行一个 JSON 对象"""
    data = []
    with open(file_path, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if line:
                try:
                    data.append(json.loads(line))
                except json.JSONDecodeError:
                    continue
    return data

# 从 content 字段中提取各个部分
def parse_content(content):
    """从 content 字符串中提取各个分析字段"""
    result = {
        'meaning': '',
        'examples': '',
        'root_analysis': '',
        'affix_analysis': '',
        'history_culture': '',
        'word_variations': '',
        'memory_aid': '',
        'story': ''
    }

    # 定义提取规则（按标题匹配）
    patterns = {
        'meaning': r'###\s*分析词义\s*\n(.*?)(?=###\s*列举例句|$)',
        'examples': r'###\s*列举例句\s*\n(.*?)(?=###\s*词根分析|###\s*分析词义|$)',
        'root_analysis': r'###\s*词根分析\s*\n(.*?)(?=###\s*词缀分析|###\s*列举例句|$)',
        'affix_analysis': r'###\s*词缀分析\s*\n(.*?)(?=###\s*发展历史和文化背景|###\s*词根分析|$)',
        'history_culture': r'###\s*发展历史和文化背景\s*\n(.*?)(?=###\s*单词变形|###\s*词缀分析|$)',
        'word_variations': r'###\s*单词变形\s*\n(.*?)(?=###\s*记忆辅助|###\s*发展历史和文化背景|$)',
        'memory_aid': r'###\s*记忆辅助\s*\n(.*?)(?=###\s*小故事|###\s*单词变形|$)',
        'story': r'###\s*小故事\s*\n(.*?)(?=###\s*|$)'
    }

    for key, pattern in patterns.items():
        match = re.search(pattern, content, re.DOTALL)
        if match:
            # 清理内容：去除多余空白和换行
            text = match.group(1).strip()
            # 将多个连续换行替换为单个换行
            text = re.sub(r'\n\s*\n', '\n', text)
            result[key] = text

    return result

def main():
    # 输入输出文件路径
    input_file = 'gptwords.json'
    output_file = 'vocabulary.xlsx'

    # 读取数据
    print(f"正在读取文件: {input_file}")
    raw_data = read_json_file(input_file)
    print(f"共读取 {len(raw_data)} 条记录")

    # 解析每条记录
    records = []
    for item in raw_data:
        word = item.get('word', '')
        content = item.get('content', '')

        parsed = parse_content(content)

        records.append({
            '单词': word,
            '词义分析': parsed['meaning'],
            '例句': parsed['examples'],
            '词根分析': parsed['root_analysis'],
            '词缀分析': parsed['affix_analysis'],
            '发展历史和文化背景': parsed['history_culture'],
            '单词变形': parsed['word_variations'],
            '记忆辅助': parsed['memory_aid'],
            '小故事': parsed['story']
        })

    # 创建 DataFrame
    df = pd.DataFrame(records)

    # 保存为 Excel 文件
    print(f"正在保存到: {output_file}")
    with pd.ExcelWriter(output_file, engine='openpyxl') as writer:
        df.to_excel(writer, sheet_name='单词表', index=False)

        # 调整列宽
        worksheet = writer.sheets['单词表']
        for column in worksheet.columns:
            max_length = 0
            column_letter = column[0].column_letter
            for cell in column:
                try:
                    if len(str(cell.value)) > max_length:
                        max_length = len(str(cell.value))
                except:
                    pass
            adjusted_width = min(max_length + 2, 50)
            worksheet.column_dimensions[column_letter].width = adjusted_width

    print(f"转换完成！文件已保存为: {output_file}")

if __name__ == '__main__':
    main()