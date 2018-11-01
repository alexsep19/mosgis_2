define ([], function () {
    
    var grid_name = 'house_property_documents_grid'
                
    return function (data, view) {
    
        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                footer: 1,
                toolbarReload: false,
                toolbarColumns: false,
            },            

            textSearch: 'contains',

            columns: [                
                {field: 'dt', caption: 'Дата', size: 18, render:_dt},
                {field: 'no', caption: '№', size: 25},
            ],
            
            postData: {data: {"uuid_house": $_REQUEST.id}},

            url: '/mosgis/_rest/?type=property_documents',
                                                                        
        })

    }
    
})