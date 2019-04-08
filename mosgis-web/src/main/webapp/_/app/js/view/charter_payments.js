define ([], function () {
    
    var grid_name = 'charter_payments_grid'
                
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
                {field: 'payment_1', caption: 'Руб/м2 (для членов)', size: 10},
                {field: 'payment_0', caption: 'Руб/м2 (для не членов)', size: 10},
            ],
            
            postData: {data: {uuid_charter: $_REQUEST.id}},

            url: '/_back/?type=charter_payments',
                                    
            onDblClick: function (e) {openTab ('/charter_payment/' + e.recid)},
            
            onAdd: $_DO.create_charter_payments,
            
        })

    }
    
})