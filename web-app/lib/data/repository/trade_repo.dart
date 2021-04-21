import 'package:cordatradeclient/data/repository/trade_api_client.dart';
import 'package:cordatradeclient/models/trade_state_model.dart';

class TradeRepository {
  late TradeApiClient _tradeApiClient;

  TradeRepository({required TradeApiClient tradeApiClient})
      : _tradeApiClient = tradeApiClient;

  Future<String?> getMyInformation(
      String domain, String port, String username, String password) async {
    try {
      return _tradeApiClient.whoAmI(domain, port, username, password);
    } catch (e) {}
  }

  Future<List<String>?> getPeers() => _tradeApiClient.getPeers();

  Future<bool> issueTrade(int amount, String issueTo) =>
      _tradeApiClient.issueTrade(amount, issueTo);

  Future<bool> tradeInProcess(String id) => _tradeApiClient.tradeInProcess(id);

  Future<bool> tradeSettled(String id) => _tradeApiClient.tradeSettled(id);

  Future<List<TradeStateModel>?> getTrades({String? id, String? status}) =>
      _tradeApiClient.getTraders(id: id, status: status);
}
