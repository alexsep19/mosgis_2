define ([], function () {
    
    var grid_name = 'account_payment_documents_grid'
                
    return function (data, view) {
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))
        
        var it = data.item

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: 1,
                footer: 1,
                toolbarAdd: it._can.create_payment_documents,
                toolbarReload: true,
                toolbarColumns: false,
                toolbarInput: true,
            },            
/*            
            toolbar: {
            
                items: [
                    {type: 'button', id: 'ind', caption: 'Добавить ЛС физического лица', onClick: $_DO.create_account_payment_documents, off: !data.item._can.create_account, icon: 'w2ui-icon-plus'},
                    {type: 'button', id: 'org', caption: 'Добавить ЛС юридического лица', onClick: $_DO.create_account_payment_documents, off: !data.item._can.create_account, icon: 'w2ui-icon-plus'},
                ].filter (not_off),
                
            },
*/            
            textSearch: 'contains',

            columnGroups : [
                {span: 3, caption: 'Платёжный документ'},
                {span: 2, caption: 'На начало периода'},
                {master: true},
                {master: true},
                {master: true},
                {master: true},
            ],            
            
            searches: [            
                {field: 'dt_period', caption: 'Период',         type: 'date', operator: 'between', operators: ['between']},
                {field: 'id_ctr_status', caption: 'Статус',     type: 'enum', options: {items: data.vc_gis_status.items}},
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},
//                {field: 'uuid_org', caption: 'Организации', type: 'enum', options: {items: data.vc_orgs.items}, off: !$_USER.role.admin},
            ].filter (not_off),            
            
            columns: [              
            
                {field: 'paymentdocumentnumber', caption: 'Номер', size: 20},
                {field: 'id_type', caption: 'Тип', size: 10, voc: data.vc_pay_doc_types},

                {field: 'dt_period', caption: 'Период', size: 22, render: function (r) {
                    return w2utils.settings.fullmonths [r.month - 1] + ' ' + r.year
                }},

                {field: 'debtpreviousperiods_m', caption: 'Переплата', size: 10, render: function (r) {
                    return r.debtpreviousperiods < 0 ? - r.debtpreviousperiods : ''
                }},

                {field: 'debtpreviousperiods_p', caption: 'Задолженность', size: 10, render: function (r) {
                    return r.debtpreviousperiods > 0 ?   r.debtpreviousperiods : ''
                }},
                
                {field: 'totalpayablebypd', caption: 'Сумма документа, руб.', size: 10, render: 'float:2'},
                {field: 'advancebllingperiod', caption: 'Оплачено в периоде, руб.', size: 10, render: 'float:2'},
                {field: 'totalpayablebypdwith_da', caption: 'К оплате по документу, руб.', size: 10, render: 'float:2'},
                
                {field: 'id_ctr_status', caption: 'Статус', size: 15, voc: data.vc_gis_status},

            ],
            
            postData: {data: {uuid_account: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=payment_documents',
                                    
            onDblClick: function (e) {openTab ('/payment_document/' + e.recid)},
            
            onAdd: $_DO.create_account_payment_documents,
            
        })

    }
    
})