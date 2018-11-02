define ([], function () {
    
    var grid_name = 'house_property_documents_grid'
                
    return function (data, view) {
    
        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({
        
            toolbar: {
            
                items: [
                    {type: 'button', id: 'create_person', caption: 'Физическое лицо', icon: 'w2ui-icon-plus', onClick: $_DO.create_person_house_property_documents},
                    {type: 'button', id: 'create_org', caption: 'Юридическое лицо', icon: 'w2ui-icon-plus', onClick: $_DO.create_org_house_property_documents},
                ].filter (not_off),
                
            },         
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                footer: 1,
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