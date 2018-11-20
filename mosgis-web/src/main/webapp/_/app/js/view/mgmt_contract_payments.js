define ([], function () {
    
    var grid_name = 'mgmt_contract_payments_grid'
                
    return function (data, view) {
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: data.item._can.create_payment,
                footer: 1,
                toolbarReload: false,
                toolbarColumns: false,
                toolbarInput: false,
                toolbarAdd: data.item._can.create_payment,
            },            

            textSearch: 'contains',
            
            columns: [              
                {field: 'begindate', caption: 'Начало', size: 10, render: _dt},
                {field: 'enddate', caption: 'Окончание', size: 10, render: _dt},
                {field: 'vc_buildings.label', caption: 'Адрес', size: 50},
                {field: 'housemanagementpaymentsize', caption: 'Руб/м2', size: 10},
                {field: 'type_', caption: 'Основание', size: 50, voc: data.vc_ctr_pay_types},
            ],
            
            postData: {data: {uuid_contract: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=contract_payments',
                                    
            onDblClick: function (e) {openTab ('/mgmt_contract_payment/' + e.recid)},
            
            onAdd: $_DO.create_mgmt_contract_payments,
            
        })

    }
    
})