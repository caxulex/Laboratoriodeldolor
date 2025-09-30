#!/usr/bin/env python3
import re
import os
import xml.etree.ElementTree as ET
from collections import defaultdict

def extract_string_ids_from_code():
    """Extract all R.string.* references from Kotlin files"""
    string_ids = set()
    pattern = r'R\.string\.([a-zA-Z_][a-zA-Z0-9_]*)'
    
    # Walk through all Kotlin files
    for root, dirs, files in os.walk('app/src/main/java'):
        for file in files:
            if file.endswith('.kt'):
                file_path = os.path.join(root, file)
                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        content = f.read()
                        matches = re.findall(pattern, content)
                        string_ids.update(matches)
                except Exception as e:
                    print(f"Error reading {file_path}: {e}")
    
    return sorted(string_ids)

def extract_string_ids_from_xml(xml_path):
    """Extract string IDs from strings.xml file"""
    try:
        tree = ET.parse(xml_path)
        root = tree.getroot()
        string_ids = set()
        
        for string_elem in root.findall('string'):
            name = string_elem.get('name')
            if name:
                string_ids.add(name)
                
        for plurals_elem in root.findall('plurals'):
            name = plurals_elem.get('name')
            if name:
                string_ids.add(name)
        
        return sorted(string_ids)
    except Exception as e:
        print(f"Error parsing {xml_path}: {e}")
        return []

def main():
    print("=== AUDITORÍA DE RECURSOS DE STRINGS ===\n")
    
    # Extract string IDs used in code
    code_string_ids = extract_string_ids_from_code()
    print(f"1. String IDs encontrados en el código Kotlin: {len(code_string_ids)}")
    
    # Extract string IDs from resource files
    xml_files = [
        'app/src/main/res/values/strings.xml',
        'app/src/main/res/values-es/strings.xml', 
        'app/src/main/res/values-en/strings.xml'
    ]
    
    xml_strings = {}
    for xml_file in xml_files:
        if os.path.exists(xml_file):
            xml_strings[xml_file] = extract_string_ids_from_xml(xml_file)
            print(f"2. String IDs en {xml_file}: {len(xml_strings[xml_file])}")
        else:
            print(f"❌ Archivo no encontrado: {xml_file}")
    
    print("\n=== ANÁLISIS DE CONSISTENCIA ===")
    
    # Check for missing strings
    default_strings = xml_strings.get('app/src/main/res/values/strings.xml', set())
    missing_in_default = set(code_string_ids) - set(default_strings)
    
    if missing_in_default:
        print(f"\n❌ String IDs usados en código pero NO encontrados en values/strings.xml ({len(missing_in_default)}):")
        for missing_id in sorted(missing_in_default):
            print(f"   - {missing_id}")
    else:
        print("\n✅ Todos los string IDs del código están presentes en values/strings.xml")
    
    # Check for unused strings
    unused_strings = set(default_strings) - set(code_string_ids)
    if unused_strings:
        print(f"\n⚠️  String IDs definidos pero NO usados en código ({len(unused_strings)}):")
        for unused_id in sorted(unused_strings):
            print(f"   - {unused_id}")
    else:
        print("\n✅ Todos los strings definidos están siendo usados")
    
    # Check consistency between locales
    print("\n=== CONSISTENCIA ENTRE IDIOMAS ===")
    for xml_file, strings in xml_strings.items():
        if xml_file != 'app/src/main/res/values/strings.xml':
            missing_in_locale = set(default_strings) - set(strings)
            extra_in_locale = set(strings) - set(default_strings)
            
            locale = xml_file.split('/')[-2]  # Extract locale from path
            print(f"\n{locale}:")
            
            if missing_in_locale:
                print(f"  ❌ Faltan traducciones ({len(missing_in_locale)}):")
                for missing in sorted(list(missing_in_locale)[:10]):  # Show first 10
                    print(f"     - {missing}")
                if len(missing_in_locale) > 10:
                    print(f"     ... y {len(missing_in_locale) - 10} más")
            else:
                print(f"  ✅ Todas las traducciones presentes")
                
            if extra_in_locale:
                print(f"  ⚠️  Strings extra no en default ({len(extra_in_locale)}):")
                for extra in sorted(list(extra_in_locale)[:5]):  # Show first 5
                    print(f"     - {extra}")
                if len(extra_in_locale) > 5:
                    print(f"     ... y {len(extra_in_locale) - 5} más")
    
    print(f"\n=== RESUMEN FINAL ===")
    print(f"• Total string IDs en código: {len(code_string_ids)}")
    print(f"• Total string IDs en default: {len(default_strings)}")
    print(f"• String IDs faltantes: {len(missing_in_default)}")
    print(f"• String IDs no usados: {len(unused_strings)}")
    
    if len(missing_in_default) == 0 and len(unused_strings) == 0:
        print("\n✅ AUDIT COMPLETO: Todos los strings están correctamente configurados")
        return True
    else:
        print(f"\n⚠️  AUDIT INCOMPLETO: Se encontraron {len(missing_in_default) + len(unused_strings)} problemas")
        return False

if __name__ == "__main__":
    main()