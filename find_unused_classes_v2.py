import os
import re

def find_java_files(directory):
    java_files = []
    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith(".java"):
                java_files.append(os.path.join(root, file))
    return java_files

def get_class_name(file_path):
    with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()
        match = re.search(r'public\s+(?:class|interface|enum)\s+(\w+)', content)
        if match:
            return match.group(1)
    return None

def find_unused_classes(directory):
    java_files = find_java_files(directory)
    class_files = {get_class_name(f): f for f in java_files if get_class_name(f)}

    unused_classes = []

    for class_name, file_path in class_files.items():
        is_used = False
        for other_file_path in java_files:
            if file_path == other_file_path:
                continue

            with open(other_file_path, 'r', encoding='utf-8', errors='ignore') as f:
                content = f.read()
                if re.search(r'\b' + re.escape(class_name) + r'\b', content):
                    is_used = True
                    break

        if not is_used:
            unused_classes.append(file_path)

    return unused_classes

if __name__ == "__main__":
    unused = find_unused_classes(".")
    if unused:
        print("Potentially unused classes:")
        for file_path in unused:
            print(file_path)
    else:
        print("No unused classes found.")