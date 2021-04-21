import 'package:cordatradeclient/utils/party_ext.dart';
import 'package:test/test.dart';

void main() {
  final party = "O=PartyA, L=London, C=GB";
  group('Counter', () {
    test('Get Organization', () {
      expect(party.getOrganization() ?? "", "PartyA");
    });

    test('Get Locality', () {
      expect(party.getLocation(), "London");
    });

    test('Get Country', () {
      expect(party.getCountry(), "GB");
    });
  });
}
