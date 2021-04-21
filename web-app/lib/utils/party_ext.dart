extension PartyParsing on String {
  String? getOrganization() {
    return _getValue("O");
  }

  String? getLocation() {
    return _getValue("L");
  }

  String? getCountry() {
    return _getValue("C");
  }

  String? _getValue(String key) {
    final items = this.trim().split(",");
    for (int i = 0; i < items.length; i++) {
      final value = items[i];
      if (value.contains("$key=")) {
        return value.substring(value.indexOf("=") + 1, value.length);
      }
    }
  }
// ···
}
