define ([], function () {

    return function (data, view) {
    
        var it = data.item       
            
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'metering_device_common_accounts_grid',

            show: {
                toolbar: true,
                toolbarAdd: it._can.edit,
                toolbarInput: false,
                footer: true,
            },     
/*
            columnGroups : [            
                {span: 3, caption: 'Событие'},                
                {span: 10, caption: 'Значения полей'},                    
                {span: 3, caption: 'Запрос в ГИС ЖКХ'},                
            ], 
*/            
            columns: [                
                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
/*                
                {field: 'action', caption: 'Действие',    size: 30, voc: data.vc_actions},
                {field: 'vc_users.label', caption: 'Оператор',    size: 30},

                {field: 'code_vc_nsi_27', caption: 'Тип', size: 10, voc: data.vc_nsi_27},
                {field: 'premise.label', caption: 'Помещение', size: 5},                
                {field: 'meteringdevicenumber', caption: '№', size: 10},
                {field: 'meteringdevicestamp', caption: 'Марка', size: 10},
                {field: 'meteringdevicemodel', caption: 'Модель', size: 10},
                {field: 'mask_vc_nsi_2', caption: 'Ресурс', size: 10, voc: data.vc_nsi_2},                
                {field: 'installationdate', caption: 'Установлен', tooltip: 'Дата установки', size: 18, render: _dt},
                {field: 'commissioningdate', caption: 'Введён', tooltip: 'Дата ввода в эксплуатацию', size: 18, render: _dt},
                {field: 'remotemeteringinfo', caption: 'Дист.', tooltip: 'Система дистанционного снятия показаний', size: 10},
                {field: 'id_ctr_status', caption: 'Статус', size: 50, voc: data.vc_gis_status},
                
                {field: 'soap.ts', caption: 'Отправлено',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.ts_rp', caption: 'Обработано',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.err_text', caption: 'Ошибка',    size: 30},
*/
            ],
            
            records: [],
            
            onAdd: $_DO.create_metering_device_common_accounts,
            
        }).refresh ();

    }

})