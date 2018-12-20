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
                toolbarReload: false,
                toolbarColumns: false,
                toolbarInput: false,
                toolbarAdd: data.item._can.create_payment,
                toolbarDelete: data.item._can.create_payment,
            },            

            textSearch: 'contains',
            
            columns: [              
                {field: 'datefrom', caption: 'Начало', size: 10, render: _dt},
                {field: 'dateto', caption: 'Окончание', size: 10, render: _dt},
                {field: 'bill', caption: 'Начислено', size: 10},
                {field: 'debt', caption: 'Задолженность/переплата', size: 10},
                {field: 'paid', caption: 'Оплачено', size: 10},
            ],
            
            postData: {data: {uuid_ctr: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=agreement_payments',
                                    
//            onDblClick: function (e) {openTab ('/public_property_contract_agreement_payment/' + e.recid)},
            
            onAdd: $_DO.create_public_property_contract_agreement_payments,
            
            onDelete: $_DO.delete_public_property_contract_agreement_payments,
            
        })

    }
    
})