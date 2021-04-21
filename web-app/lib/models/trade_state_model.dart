class TradeStateModel {
  TradeState state;
  Ref ref;

  TradeStateModel({required this.state, required this.ref});

  factory TradeStateModel.fromJson(Map<String, dynamic> json) {
    return TradeStateModel(
        state: new TradeState.fromJson(json['state']),
        ref: Ref.fromJson(json['ref']));
  }
}

class TradeState {
  Data data;
  String contract;
  String notary;

  TradeState(
      {required this.data, required this.contract, required this.notary});

  factory TradeState.fromJson(Map<String, dynamic> json) {
    return TradeState(
        data: new Data.fromJson(json['data']),
        contract: json['contract'],
        notary: json['notary']);
  }
}

class Data {
  final int amount;
  final String assignedBy;
  final String assignedTo;
  final String date;
  final String tradeStatus;
  final List<String> participants;
  final String linearId;

  Data(
      {required this.amount,
      required this.assignedBy,
      required this.assignedTo,
      required this.date,
      required this.tradeStatus,
      required this.participants,
      required this.linearId});

  factory Data.fromJson(Map<String, dynamic> json) {
    return Data(
        amount: json['amount'],
        assignedBy: json['assignedBy'],
        assignedTo: json['assignedTo'],
        date: json['date'],
        tradeStatus: json['tradeStatus'],
        participants: json['participants'].cast<String>(),
        linearId: json['linearId']["id"]);
  }
}

class Ref {
  final String txhash;
  final int index;

  Ref({required this.txhash, required this.index});

  factory Ref.fromJson(Map<String, dynamic> json) {
    return Ref(
      txhash: json['txhash'] as String,
      index: json['index'] as int,
    );
  }
}
