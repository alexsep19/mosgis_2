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
                toolbarInput: false,
                toolbar: true,
                footer: 1,
            },            

            textSearch: 'contains',

            columns: [                
                {field: 'premise.label', caption: 'Помещение', size: 10},
                {field: 'label', caption: 'Собственник', size: 30, render: function (r) {return r ['org.label'] || r ['person.label'] }},
                {field: 'premise.totalarea', caption: 'Площадь, м2', size: 10},
                {field: 'prc', caption: 'Доля, %', size: 10},
                {field: 'id_type', caption: 'Документ', size: 25, voc: data.vc_prop_doc_types},
                {field: 'no', caption: '№', size: 25},
                {field: 'dt', caption: 'Дата', size: 18, render:_dt},
            ],
            
            postData: {data: {"uuid_house": $_REQUEST.id}},

            url: '/mosgis/_rest/?type=property_documents',
                                                                        
        })

    }
    
})