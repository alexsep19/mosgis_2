define ([], function () {

    function _ddt (record, ind, col_ind, data) {
        return data < 99 ? data : 'посл.'
    }
    
    var nxt = {
        0: 'тек.',
        1: 'след.',
    }

    return function (data, view) {
    
        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({ 

            name: 'voc_organization_legal_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },     

            columns: [                
            
                {field: 'type_', caption: 'Тип', size: 50, voc: data.vc_acc_req_types},
                {field: 'applicationdate', caption: 'Дата применения', size: 18, render: _dt},
                {field: 'startdate', caption: 'Дата начала', size: 18, render: _dt},
                {field: 'enddate', caption: 'Дата окончания', size: 18, render: _dt},
                {field: 'status', caption: 'Статус', size: 50, voc: data.vc_acc_req_status},
                {field: 'statuschangedate', caption: 'Дата изменения статуса', size: 18, render: _dt},
                {field: 'statusreason', caption: 'Причина статуса', size: 50},

            ],

            records: data.records,
            
            onClick: function (e) {
            
                var c = this.columns [e.column]
                var r = this.get (e.recid)
                
                if ($_USER.role.admin) switch (c.field) {
                    case 'soap.ts':    if (r.uuid_out_soap) return openTab ('/out_soap_rq/' + r.uuid_out_soap)
                    case 'soap.ts_rp': if (r ['soap.uuid_ack']) return openTab ('/out_soap_rp/' + r ['soap.uuid_ack'])
                }
            
            },
            
        }).refresh ();

        w2ui['voc_organization_legal_log'].on('columnOnOff:after', function () {
            this.resize()
        })
    }

})