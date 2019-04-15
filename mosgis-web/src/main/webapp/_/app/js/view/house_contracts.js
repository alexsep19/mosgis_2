define ([], function () {

    var grid_name = 'house_contracts_grid'

    return function (data, view) {

        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({ 

            multiSelect: false,

            name: grid_name,

            show: {
                footer: 1,
                toolbarReload: false,
                toolbarColumns: false,
                toolbarInput: false,
            },            

            textSearch: 'contains',

            columns: [                
                {field: 'dt', caption: 'Дата', size: 18},
                {field: 'no', caption: 'Номер', size: 10},
            ],            

            records: darn (data.lines)

//            onDblClick: $_DO.download_house_contracts,

        })

    }

})