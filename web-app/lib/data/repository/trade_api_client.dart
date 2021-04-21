import 'dart:convert';

import 'package:cordatradeclient/models/trade_state_model.dart';
import 'package:http/http.dart' as http;

class TradeApiClient
{
  late String domain, port, username, password;

  late Map<String, String> headers = {
    'content-type': 'application/json',
    'accept': 'application/json',
    'authorization': 'Basic ' + base64Encode(utf8.encode('$username:$password'))
  };

  Future<String?> whoAmI(String domain, String port, String username, String password) async {
    try {
      this.domain = domain;
      this.port = port;
      this.username = username;
      this.password = password;

      var url = Uri.http('$domain:$port', '/api/me');
      final response = await http.get(url, headers: headers);
      if (response.statusCode == 200) {
        return jsonDecode(response.body)["me"];
      }
      return null;
    } catch (e) {
      print(e);
    }
  }

  Future<List<String>?> getPeers() async {
    try {
      var url = Uri.http('$domain:$port', '/api/peers');
      final response = await http.get(url, headers: headers);
      if (response.statusCode == 200) {
        return jsonDecode(response.body)["peers"].cast<String>();
      }
      return null;
    } catch (e) {
      print(e);
    }
  }

  Future<bool> issueTrade(int amount, String issueTo) async {
    try {
      var url = Uri.http('$domain:$port', '/api/issueTrade/$issueTo');
      final response = await http.post(url,
          headers: headers, body: jsonEncode({"amount": amount}));
      if (response.statusCode == 201) {
        return true;
      }
      return false;
    } catch (e) {
      print(e);
      return false;
    }
  }

  Future<bool> tradeInProcess(String id) async {
    try {
      var url = Uri.http('$domain:$port', '/api/processTrade');
      final response = await http.post(url,
          headers: headers, body: jsonEncode({"linearId": id}));
      if (response.statusCode == 201) {
        return true;
      }
      return false;
    } catch (e) {
      print(e);
      return false;
    }
  }

  Future<bool> tradeSettled(String id) async {
    try {
      var url = Uri.http('$domain:$port', '/api/settle');
      final response = await http.post(url,
          headers: headers, body: jsonEncode({"linearId": id}));
      if (response.statusCode == 201) {
        return true;
      }
      return false;
    } catch (e) {
      print(e);
      return false;
    }
  }

  Future<List<TradeStateModel>?> getTraders(
      {String? id, String? status}) async {
    try {
      final queryParams = Map<String, dynamic>();
      if (id != null) {
        queryParams["id"] = id;
      } else if (status != null) {
        queryParams["status"] = status;
      }

      var url = Uri.http('$domain:$port', '/api/traders', queryParams);
      final response = await http.get(url, headers: headers);
      if (response.statusCode == 200) {
        return jsonDecode(response.body)
            .map((data) => TradeStateModel.fromJson(data))
            .toList()
            .cast<TradeStateModel>();
      } else {
        return null;
      }
    } catch (e) {
      print(e);
      return null;
    }
  }

  static final TradeApiClient instance = TradeApiClient._internal();

  TradeApiClient._internal() {}
}