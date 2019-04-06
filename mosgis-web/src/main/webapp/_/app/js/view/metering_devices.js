define ([], function () {

    var grid_name = 'metering_devices_grid'

    return function (data, view) {
    
        var it = data.item

        var postData = {data: {}}

//        if (is_author) 
            postData.data.uuid_org = $_USER.uuid_org
    
        $(w2ui ['rosters_layout'].el ('main')).w2regrid ({
                
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                footer: 1,
            },            

            toolbar: {
            
                items: [
                
                    {
                        type: 'button',
                        id: 'import_objects',
                        caption: 'Импорт ПУ...',
                        icon: 'w2ui-icon-plus',
                        onClick: $_DO.import_metering_devices,
                        off: !$_USER.uuid_org,
                    },
                    
                ].filter (not_off),
                
            },                        

            textSearch: 'contains',

            searches: [            
                {field: 'code_vc_nsi_27', caption: 'Тип ПУ', type: 'enum', options: {items: data.vc_nsi_27.items}},
                {field: 'mask_vc_nsi_2', caption: 'Учитываемый ресурс', type: 'enum', options: {items: data.vc_nsi_2.items}},
                {field: 'meteringdevicenumber',  caption: 'Заводской (серийный) №',  type: 'text'},
            ].filter (not_off),

            columns: [                
            
                {field: 'address', caption: 'Адрес', size: 30},
                {field: 'code_vc_nsi_27', caption: 'Тип', size: 10, voc: data.vc_nsi_27},
                {field: 'premise.label', caption: 'Помещение', size: 5},                
                {field: 'meteringdevicenumber', caption: '№', size: 10},
                {field: 'meteringdevicestamp', caption: 'Марка', size: 10},
                {field: 'meteringdevicemodel', caption: 'Модель', size: 10},
                {field: 'mask_vc_nsi_2', caption: 'Ресурс', size: 10, voc: data.vc_nsi_2},
                {field: 'commissioningdate', caption: 'Введён', tooltip: 'Дата ввода в эксплуатацию', size: 18, render: _dt},
                {field: 'id_ctr_status', caption: 'Статус', size: 50, voc: data.vc_gis_status},
                
            ].filter (not_off),
            
            postData: postData,

            url: '/_back/?type=metering_devices',
            
            onDblClick: function (e) {openTab ('/metering_device/' + e.recid)},
            
            onAdd: $_DO.create_metering_devices,
                                                                        
        })

    }
    
})