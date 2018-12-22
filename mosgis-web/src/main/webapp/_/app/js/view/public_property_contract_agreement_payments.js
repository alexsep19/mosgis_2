define ([], function () {
    
    var grid_name = 'public_property_contract_agreement_payments_grid'
    
    function recalcToolbar (e) {e.done (function () {
    
        var g = w2ui [grid_name]

        var t = g.toolbar
        var r = g.get (g.getSelection () [0])

        var id_status = r ? r.id_ap_status : -1

        switch (id_status) {        
            case 10:
            case 11:
                t.enable ('approve')
                break
            default:
                t.disable ('approve')                
        }
        
        switch (id_status) {        
            case 10:
            case 14:
                t.enable ('delete')
                break
            default:
                t.disable ('delete')
        }        
        
        switch (id_status) {        
            case 10:
            case 11:
            case 14:
            case 40:
                t.enable ('edit')
                break
            default:
                t.disable ('edit')
        }        

    })}

                
    return function (data, view) {
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: 1,
                footer: 1,
                toolbarSearch: true,
                toolbarReload: false,
                toolbarColumns: false,
                toolbarInput: false,
                toolbarAdd: data.item._can.create_payment,
                toolbarDelete: false, //data.item._can.create_payment,
                toolbarEdit: false, //data.item._can.create_payment,
            },            
            
            
            toolbar: {
            
                items: !data.item._can.create_payment ? null : [
                    {type: 'button', id: 'edit', caption: 'Изменить', onClick: $_DO.edit_public_property_contract_agreement_payments, disabled: true, icon: 'w2ui-icon-pencil'},
                    {type: 'button', id: 'delete', caption: 'Удалить', onClick: $_DO.delete_public_property_contract_agreement_payments, disabled: true, icon: 'w2ui-icon-cross'},
                    {type: 'button', id: 'approve', caption: 'Разместить', onClick: $_DO.approve_public_property_contract_agreement_payments, disabled: true, off: $_USER.role.admin},
                ].filter (not_off),
                
            }, 

            textSearch: 'contains',
            
            searches: [            
                {field: 'datefrom', caption: 'Начало периода', type: 'date'},
                {field: 'dateto', caption: 'Окончание периода', type: 'date'},
                {field: 'id_ap_status', caption: 'Статус', type: 'enum', options: {items: data.vc_gis_status.items}},
            ].filter (not_off),

            columns: [              
                {field: 'datefrom', caption: 'Начало', size: 15, render: _dt},
                {field: 'dateto', caption: 'Окончание', size: 15, render: _dt},
                {field: 'bill', caption: 'Начислено', size: 20, render: 'money:2'},
                {field: 'paid', caption: 'Оплачено', size: 20, render: 'money:2'},
                {field: 'debt', caption: 'Задолженность/переплата', size: 20, render: 'money:2'},
                {field: 'id_ap_status', caption: 'Статус', size: 100, voc: data.vc_gis_status},

                {field: 'soap.ts', caption: 'Отправлено',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.ts_rp', caption: 'Обработано',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.err_text', caption: 'Ошибка',    size: 30},
                
            ],
            
            postData: {data: {uuid_ctr: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=agreement_payments',
                                                
            onAdd:    $_DO.create_public_property_contract_agreement_payments,            
            onDblClick: function (e) {
                if (!data.item._can.create_payment) return
                w2ui [e.target].select (e.recid)
                $_DO.edit_public_property_contract_agreement_payments (e)
            },
            
            onRefresh: function (e) {e.done (color_data_mandatory)},

            onSelect: !data.item._can.create_payment ? null : recalcToolbar,
            onUnselect: !data.item._can.create_payment ? null : recalcToolbar,
            onClick: function (e) {
            
                var c = this.columns [e.column]
                var r = this.get (e.recid)
                
                switch (c.field) {
                    case 'soap.ts':    if (r ['soap.ts']) return openTab ('/out_soap_rq/' + r.id_log)
                    case 'soap.ts_rp': if (r ['soap.ts_rp']) return openTab ('/out_soap_rp/' + r ['soap.uuid_ack'])
                }
            
            },

        })

    }
    
})