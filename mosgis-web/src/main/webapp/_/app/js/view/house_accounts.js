define ([], function () {

    var grid_name = 'house_accounts_grid'

    return function (data, view) {    
    
        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({
                
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                footer: 1,
            },            

            textSearch: 'contains',
/*
            searches: [            
                {field: 'code_vc_nsi_27', caption: 'Тип ПУ', type: 'enum', options: {items: data.vc_nsi_27.items}},
                {field: 'mask_vc_nsi_2', caption: 'Учитываемый ресурс', type: 'enum', options: {items: data.vc_nsi_2.items}},
                {field: 'meteringdevicenumber',  caption: 'Заводской (серийный) №',  type: 'text'},
            ].filter (not_off),
*/
            columns: [                

                {field: 'id_type', caption: 'Тип', size: 10, voc: {
                    1: "Лицевой счет для оплаты за жилое помещение и коммунальные услуги",
                    2: "Лицевой счет для оплаты за коммунальные услуги",
                    3: "Лицевой счет для оплаты капитального ремонта",
                    4: "Лицевой счет ОГВ/ОМС",
                    5: "Лицевой счет РКЦ",
                }},                
                {field: 'accountnumber', caption: '№ л/с', size: 10},                
                {field: 'premises', caption: 'Помещения', size: 20},                
                {field: 'customer', caption: 'Плательщик', size: 30},                
                {field: 'serviceid', caption: 'Идентификатор поставщика услуги', size: 10},                
                {field: 'owner', caption: 'Организация - владелец ЛС', size: 10},                
                {field: 'id_ctr_status', caption: 'Статус', size: 10, voc: {
                    10:  "проект",
                    40:  "размещен",
                    100: "закрыт",
                    110: "аннулирован",
                }},                
            ].filter (not_off),
            
            records: darn(data.lines),
            
            onDblClick: function (e) {openTab ('/account/' + e.recid)},
                                                                                    
        }).refresh ()

    }
    
})