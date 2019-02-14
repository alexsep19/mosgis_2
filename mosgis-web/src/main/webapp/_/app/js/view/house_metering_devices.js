define ([], function () {

    var grid_name = 'house_metering_devices_grid'

    return function (data, view) {
    
        var it = data.item

        var is_author = $_USER.role.nsi_20_1 || $_USER.is_building_society ()

        var postData = {data: {fiashouseguid: data.item.fiashouseguid}}

        if (is_author) postData.data.uuid_org = $_USER.uuid_org
    
        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({
                
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                toolbarInput: false,
                toolbarAdd: it._can.add_metering_devices,
                footer: 1,
            },            

            textSearch: 'contains',
/*
            searches: [            
                {field: 'label',  caption: 'Номер помещения',  type: 'text'},
                {field: 'owner_label_uc',  caption: 'Собственник',  type: 'text'},
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},
            ].filter (not_off),
*/            
            columns: [                
                {field: 'label', caption: '???', size: 10},
/*            
                {field: 'owner_label', caption: 'Собственник', size: 30},
                {field: 'totalarea', caption: 'Площадь, м2', size: 10},
                {field: 'prc', caption: 'Доля, %', size: 10},
                {field: 'id_type', caption: 'Документ', size: 25, voc: data.vc_prop_doc_types},
                {field: 'no', caption: '№', size: 25},
                {field: 'dt', caption: 'Дата', size: 18, render:_dt},
                {field: 'author_label', caption: 'Поставщик информации', size: 30, off: is_author},
*/                
            ].filter (not_off),
            
            postData: postData,

            url: '/mosgis/_rest/?type=metering_devices',
            
            onDblClick: function (e) {openTab ('/metering_device/' + e.recid)},
            
            onAdd: $_DO.create_house_metering_devices,
                                                                        
        })

    }
    
})