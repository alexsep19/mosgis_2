define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')

        $(w2ui ['vocs_layout'].el ('main')).w2regrid ({

            name: 'voc_differentiation_grid',

            multiSelect: false,

            show: {
                toolbar: true,
                toolbarSearch: true,
            },
            
            toolbar: {

                items: [
                    {type: 'button', id: 'edit', caption: 'Обновить', onClick: $_DO.import_voc_differentiation, icon: 'w2ui-icon-pencil'},
                ],
            
            },
/*
            searches: [
                {field: 'bic', caption: 'БИК', type: 'text'},
                {field: 'account', caption: '№ корреспондентского счёта', type: 'text'},
                {field: 'namep', caption: 'Наименование', type: 'text'},
                {field: 'regn', caption: 'Рег. №', type: 'text'},
                {field: 'datein', caption: 'от', type: 'text'},
                {field: 'code_vc_nsi_237', caption: 'Регион', type: 'enum', options: {items: data.vc_nsi_237.items}},
            ],
            
Тип тарифа (UsedFor) - множественное значение. Отобразить через запятую, при наведении на ячейку отобразить подсказку с полным списком.
MunicipalResourceTariff - тариф на оплату коммунальных ресурсов,
ResidentialPremisesMaintenance - размер платы за содержание жилого помещения,
CapitalRepairTariff - минимальный размер взноса на капитальный ремонт,
SocialNorm - социальная норма потребления электрической энергии,
ResidentialPremisesUsage - размер платы за пользование жилым помещением).
Вид тарифа (TariffKind) - из справочника НСИ 268 (Вид тарифа). Заполняется только для тарифов на коммунальные ресурсы. Содержит сведения о виде тарифа на коммунальные ресурсы, для которого может быть применен критерий дифференциации. Отобразить через запятую наименования, при наведении на ячейку отобразить подсказку с полным списком.
            
*/
            columns: [                
                {field: 'differentiationcode', caption: 'Код', size: 9},
                {field: 'differentiationname', caption: 'Наименование', size: 25},
                {field: 'differentiationvaluekind', caption: 'Тип значений', size: 10, voc: data.vc_diff_value_types},
                {field: 'vc_nsi_list.name', caption: 'Справочник', size: 25},
                {field: 'vc_nsi_list.name', caption: 'Значения', size: 25, render: function (r) {
                    if (!r.nsiitem) return ''
                    return (r.isplural ? 'несколько' : 'одно')
                }},
            ],
            
            records: data.records,
            
            onDblClick: null,
            
/*
            onRequest: function (e) {
            
                e.done ($_DO.check_voc_differentiation)
                
            }
*/
        }).refresh ();

    }

})