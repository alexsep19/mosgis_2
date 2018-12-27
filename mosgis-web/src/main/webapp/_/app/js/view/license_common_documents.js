define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')

        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'license_common_documents',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },
            
            columnGroups : [
                {master: true},
                {master: true},
                {master: true},
                {master: true},
                {master: true},
                {master: true},
                {master: true},
                {span: 3, caption: 'Документ-основание размещения информации'},
            ],
            
            columns: [                
                {field: 'doc_type', caption: 'Тип документа',    size: 30, voc: data.vc_nsi_75},
                {field: 'num', caption: 'Номер документа',    size: 30},
                {field: 'reg_date', caption: 'Дата', size: 18, render: _dt},
                {field: 'name', caption: 'Наименование',    size: 30},
                {field: 'org.label', caption: 'Наименование организации, принявшей решение',    size: 30},
                {field: 'id_status', caption: 'Статус',    size: 15, voc: data.vc_document_status},
                {field: 'date_from', caption: 'Дата вступления в силу', size: 18, render: _dt},
                {field: 'base_doc_name', caption: 'Наименование', size: 30, render: function (r, i, c, v) {
                        var label = r.base_doc_name ? r.base_doc_name + ' ' : ''
                        label += r.base_doc_number ? '№' + r.base_doc_number + ' ': ''
                        label += r.base_doc_date ? 'от ' + dt_dmy(r.base_doc_date) : ''
                        return label
                }},
                {field: 'base_doc_type', caption: 'Основание',    size: 30, voc: data.vc_nsi_75},
                {field: 'base_doc_decisionorg', caption: 'Организация, принявшая решение',    size: 30},
            ],
            
            url: '/mosgis/_rest/?type=licenses&part=documents&id=' + $_REQUEST.id,

        }).refresh ();

    }

})