define ([], function () {
    
    var grid_name = 'public_property_contract_agreement_payments_grid'
                
    return function (data, view) {
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: data.item._can.create_payment,
                footer: 1,
                toolbarSearch: true,
                toolbarReload: false,
                toolbarColumns: false,
                toolbarInput: false,
                toolbarAdd: data.item._can.create_payment,
                toolbarDelete: data.item._can.create_payment,
                toolbarEdit: data.item._can.create_payment,
            },            

            textSearch: 'contains',
            
            searches: [            
                {field: 'datefrom', caption: 'Начало периода', type: 'date'},
                {field: 'dateto', caption: 'Окончание периода', type: 'date'},
                {field: 'id_status', caption: 'Статус', type: 'enum', options: {items: data.vc_gis_status.items}},
            ].filter (not_off),

            columns: [              
                {field: 'datefrom', caption: 'Начало', size: 15, render: _dt},
                {field: 'dateto', caption: 'Окончание', size: 15, render: _dt},
                {field: 'bill', caption: 'Начислено', size: 20, render: 'money:2'},
                {field: 'debt', caption: 'Задолженность/переплата', size: 20, render: 'money:2'},
                {field: 'paid', caption: 'Оплачено', size: 20, render: 'money:2'},
                {field: 'id_status', caption: 'Статус', size: 100, voc: data.vc_gis_status},
            ],
            
            postData: {data: {uuid_ctr: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=agreement_payments',
                                                
            onAdd:    $_DO.create_public_property_contract_agreement_payments,            
            onDelete: $_DO.delete_public_property_contract_agreement_payments,
            onEdit:   $_DO.edit_public_property_contract_agreement_payments,
            onDblClick: function (e) {
                if (!data.item._can.create_payment) return
                w2ui [e.target].select (e.recid)
                $_DO.edit_public_property_contract_agreement_payments (e)
            },
            
            onRefresh: function (e) {e.done (color_data_mandatory)},
            
        })

    }
    
})