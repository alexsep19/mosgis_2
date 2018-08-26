define ([], function () {
                
    return function (data, view) {
                
        w2popup.open ({
            
            title: 'Информация об ошибках',
            
            width: $('body').width () - 20,
            
            onOpen: function (e) {
                
                e.done (function () {
                    
                    $('.w2ui-popup-body').w2regrid ({

                        name: 'out_soap_export_nsi_item_err_popup_grid',

                        show: {
                            toolbar: false,
                            footer: false,
                        },

                        url: '/mosgis/_rest/?type=out_soap_export_nsi_item&part=errors&id=' + data.dt,

                        columns: [
                            {field: 'ts_rp',      caption: 'Время и дата получения ошибки', size: 25, render: function (i) {return dt_dmyhms (i.ts_rp)}},
                            {field: 'id',         caption: 'Идентификатор сообщения сервиса АСУ ЕИРЦ', size: 45, attr: 'data-ref=out_soap_rq'},
                            {field: 'uuid_ack',   caption: 'Идентификатор ответа сервиса ГИС ЖКХ', size: 45, attr: 'data-ref=out_soap_rp'},
                            {field: 'err_code',   caption: 'Код ошибки', size: 15, sortable: true},
                            {field: 'err_text',   caption: 'Описание ошибки', size: 100},
                        ],

                        onDblClick: null,            

                        onClick: $_DO.open_out_soap_export_nsi_item_err_popup

                    }).refresh ()
                    
                })
            
            } 
            
        });
                
    }

});